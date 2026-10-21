package dev.tommyjs.craftreel.replay.base.handler.navigation;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCamera;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.event.PlayerJoinReplayEvent;
import dev.tommyjs.craftreel.replay.event.PlayerLeaveReplayEvent;
import dev.tommyjs.craftreel.replay.event.SpectatorCameraEvent;
import dev.tommyjs.craftreel.replay.handler.ReplayHandler;
import dev.tommyjs.craftreel.replay.reference.ReplayPlayer;
import dev.tommyjs.reel.scene.SceneHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpectatorNavigationHandler extends ReplayHandler {

    private static final int MAX_HEAD_ROWS = 5;
    private static final double FOLLOW_RANGE_SQUARED = 64D;
    private static final double EYE_HEIGHT = 1.62;
    private static final double TELEPORT_DISTANCE = 3D;
    private static final double CLEARANCE_STEP = 0.25D;
    private static final double VIEWER_HEIGHT = 1.8D;
    private static final double VIEWER_HALF_WIDTH = 0.3D;

    private final Map<UUID, UUID> following = new HashMap<>();
    private final Map<UUID, UUID> selected = new HashMap<>();
    private final Map<UUID, Integer> cameras = new HashMap<>();
    private final Map<UUID, OpenMenu> menus = new HashMap<>();
    private final Map<UUID, Long> lastClick = new HashMap<>();

    private BukkitTask followTask;
    private PacketListenerCommon interactListener;

    @Override
    protected void onLoad(SceneHandlerContext ctx) {
        for (Player viewer : getReplay().getOnlineViewers()) {
            giveCompass(viewer);
        }
        followTask = Bukkit.getScheduler().runTaskTimer(getReplay().getPlugin(), this::follow, 1L, 1L);
        interactListener = PacketEvents.getAPI().getEventManager().registerListener(new InteractListener());
    }

    @Override
    protected void onStop() {
        if (followTask != null) {
            followTask.cancel();
            followTask = null;
        }

        if (interactListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(interactListener);
            interactListener = null;
        }

        for (Player viewer : getReplay().getOnlineViewers()) {
            detach(viewer);
            clearCompass(viewer);
        }

        following.clear();
        selected.clear();
        cameras.clear();
        menus.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinReplayEvent event) {
        if (event.getReplay() == getReplay()) {
            giveCompass(event.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerLeaveReplayEvent event) {
        if (event.getReplay() != getReplay()) {
            return;
        }

        Player player = event.getPlayer();
        following.remove(player.getUniqueId());
        selected.remove(player.getUniqueId());
        menus.remove(player.getUniqueId());
        if (player.isOnline()) {
            detach(player);
            clearCompass(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isViewer(player) || !ReplayNavigation.isCompass(event.getItem())) {
            return;
        }

        event.setCancelled(true);
        if (onCooldown(player)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            openMenu(player);
        } else {
            cycle(player);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking() && isViewer(player) && following.containsKey(player.getUniqueId())) {
            setTarget(player, null);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (isViewer(player) && ReplayNavigation.isCompass(event.getCurrentItem())) {
            event.setCancelled(true);
            return;
        }

        OpenMenu menu = menus.get(player.getUniqueId());
        if (menu == null || !menu.inventory.equals(event.getInventory())) {
            return;
        }

        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= menu.inventory.getSize()) {
            return;
        }

        player.closeInventory();
        if (slot == menu.inventory.getSize() - 1) {
            setTarget(player, null);
            return;
        }

        if (slot < menu.targets.size()) {
            teleport(player, menu.targets.get(slot));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        menus.remove(event.getPlayer().getUniqueId());
    }

    protected List<ReplayPlayer> targets() {
        List<ReplayPlayer> players = new ArrayList<>();
        for (ReplayPlayer player : getReplay().getScene().getResourceManager().getResources(BaseResources.PLAYER)) {
            if (player.present() && player.location() != null) {
                players.add(player);
            }
        }

        players.sort(Comparator.comparing(ReplayPlayer::name, String.CASE_INSENSITIVE_ORDER));
        return players;
    }

    protected void cycle(Player viewer) {
        List<ReplayPlayer> targets = targets();
        if (targets.isEmpty()) {
            viewer.sendMessage(ChatColor.RED + "There is nobody to spectate yet.");
            playSound(viewer, Sound.ENDERMAN_TELEPORT, 0.5F);
            return;
        }

        UUID current = selected.get(viewer.getUniqueId());
        int next = 0;
        for (int i = 0; i < targets.size(); i++) {
            if (targets.get(i).id().equals(current)) {
                next = (i + 1) % targets.size();
                break;
            }
        }

        teleport(viewer, targets.get(next).id());
    }

    protected void teleport(Player viewer, UUID target) {
        ReplayPlayer player = getReplay().getScene().getResourceManager().find(BaseResources.PLAYER, target);
        if (player == null || player.location() == null) {
            viewer.sendMessage(ChatColor.RED + "That player is no longer in the replay.");
            return;
        }

        if (following.containsKey(viewer.getUniqueId())) {
            stopFollowing(viewer);
        }

        selected.put(viewer.getUniqueId(), target);
        teleportTo(viewer, player);
        viewer.sendMessage(ChatColor.GREEN + "Teleported to " + ChatColor.WHITE + player.name()
            + ChatColor.GREEN + ". " + ChatColor.GRAY + "Right-click them to see their view.");
        playSound(viewer, Sound.CLICK, 2F);
    }

    private void teleportTo(Player viewer, ReplayPlayer player) {
        Location target = player.location();
        if (target == null) {
            return;
        }

        Location start = target.clone();
        start.setPitch(0F);
        start.add(start.getDirection().multiply(-TELEPORT_DISTANCE));
        start.setY(target.getY() + EYE_HEIGHT);
        viewer.teleport(clearLocationToward(start, target));
    }

    private Location clearLocationToward(Location start, Location target) {
        Vector path = target.toVector().subtract(start.toVector());
        int steps = (int) Math.ceil(path.length() / CLEARANCE_STEP);
        Vector step = path.multiply(1D / steps);

        Location at = start.clone();
        for (int i = 0; i < steps; i++) {
            if (isClear(at)) {
                return at;
            }
            at.add(step);
        }

        Location end = target.clone();
        end.setPitch(start.getPitch());
        return end;
    }

    private boolean isClear(Location location) {
        World world = location.getWorld();
        int minY = location.getBlockY();
        int maxY = Location.locToBlock(location.getY() + VIEWER_HEIGHT);
        for (double dx : new double[] {-VIEWER_HALF_WIDTH, VIEWER_HALF_WIDTH}) {
            for (double dz : new double[] {-VIEWER_HALF_WIDTH, VIEWER_HALF_WIDTH}) {
                int x = Location.locToBlock(location.getX() + dx);
                int z = Location.locToBlock(location.getZ() + dz);
                for (int y = minY; y <= maxY; y++) {
                    if (world.getBlockAt(x, y, z).getType().isSolid()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected void setTarget(Player viewer, UUID target) {
        if (target == null) {
            stopFollowing(viewer);
            viewer.sendMessage(ChatColor.YELLOW + "Stopped following.");
            playSound(viewer, Sound.CLICK, 1F);
            return;
        }

        ReplayPlayer player = getReplay().getScene().getResourceManager().find(BaseResources.PLAYER, target);
        if (player == null || player.location() == null) {
            viewer.sendMessage(ChatColor.RED + "That player is no longer in the replay.");
            return;
        }

        following.put(viewer.getUniqueId(), target);
        selected.put(viewer.getUniqueId(), target);
        clearCompass(viewer);
        attach(viewer, player);
        fireCameraChange(viewer, player);
        viewer.sendMessage(ChatColor.GREEN + "Now following " + ChatColor.WHITE + player.name()
            + ChatColor.GREEN + ". " + ChatColor.GRAY + "Sneak to return to the free camera.");
        playSound(viewer, Sound.CLICK, 2F);
    }

    protected void watch(Player viewer, int entityId) {
        if (!viewer.isOnline() || !isViewer(viewer) || onCooldown(viewer)) {
            return;
        }

        for (ReplayPlayer player : targets()) {
            if (player.entityId() == entityId) {
                setTarget(viewer, player.id());
                return;
            }
        }
    }

    private void stopFollowing(Player viewer) {
        following.remove(viewer.getUniqueId());
        detach(viewer);
        giveCompass(viewer);
        fireCameraChange(viewer, null);
    }

    private void fireCameraChange(Player viewer, @Nullable ReplayPlayer target) {
        Bukkit.getPluginManager().callEvent(new SpectatorCameraEvent(getReplay(), viewer, target));
    }

    protected void openMenu(Player viewer) {
        List<ReplayPlayer> targets = targets();
        int rows = Math.min(MAX_HEAD_ROWS, Math.max(1, (targets.size() + 8) / 9));
        Inventory inventory = Bukkit.createInventory(null, (rows + 1) * 9, ChatColor.DARK_GRAY + "Spectate player");

        UUID current = following.get(viewer.getUniqueId());
        List<UUID> slots = new ArrayList<>();
        for (int i = 0; i < Math.min(targets.size(), rows * 9); i++) {
            ReplayPlayer player = targets.get(i);
            inventory.setItem(i, ReplayNavigation.head(player, player.id().equals(current)));
            slots.add(player.id());
        }

        inventory.setItem(inventory.getSize() - 1, ReplayNavigation.freeCamera(current == null));

        menus.put(viewer.getUniqueId(), new OpenMenu(inventory, slots));
        viewer.openInventory(inventory);
        playSound(viewer, Sound.CHEST_OPEN, 1F);
    }

    public void giveCompass(Player viewer) {
        viewer.getInventory().setItem(ReplayNavigation.COMPASS_SLOT, ReplayNavigation.compass());
        viewer.updateInventory();
    }

    public void clearCompass(Player viewer) {
        ItemStack item = viewer.getInventory().getItem(ReplayNavigation.COMPASS_SLOT);
        if (ReplayNavigation.isCompass(item)) {
            viewer.getInventory().setItem(ReplayNavigation.COMPASS_SLOT, null);
            viewer.updateInventory();
        }
    }

    private void follow() {
        for (Player viewer : getReplay().getOnlineViewers()) {
            UUID target = following.get(viewer.getUniqueId());
            if (target == null) {
                continue;
            }

            ReplayPlayer player = getReplay().getScene().getResourceManager().find(BaseResources.PLAYER, target);
            if (player == null || !player.present() || player.location() == null) {
                stopFollowing(viewer);
                viewer.sendMessage(ChatColor.YELLOW + "Stopped following: that player left the replay.");
                continue;
            }

            attach(viewer, player);
        }
    }

    private void attach(Player viewer, ReplayPlayer player) {
        Location to = player.location();
        if (to == null) {
            return;
        }

        Location from = viewer.getLocation();
        if (!to.getWorld().equals(from.getWorld()) || from.distanceSquared(to) > FOLLOW_RANGE_SQUARED) {
            cameras.remove(viewer.getUniqueId());
            viewer.teleport(to);
            return;
        }

        camera(viewer, player.entityId());
    }

    private void camera(Player viewer, int entityId) {
        if (entityId < 0) {
            return;
        }

        Integer attached = cameras.get(viewer.getUniqueId());
        if (attached != null && attached == entityId) {
            return;
        }

        cameras.put(viewer.getUniqueId(), entityId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, new WrapperPlayServerCamera(entityId));
    }

    private void detach(Player viewer) {
        if (cameras.remove(viewer.getUniqueId()) == null) {
            return;
        }

        PacketEvents.getAPI().getPlayerManager()
            .sendPacket(viewer, new WrapperPlayServerCamera(viewer.getEntityId()));
    }

    protected long cooldownMillis() {
        return 250L;
    }

    private boolean onCooldown(Player viewer) {
        long now = System.currentTimeMillis();
        Long last = lastClick.get(viewer.getUniqueId());
        if (last != null && now - last < cooldownMillis()) {
            return true;
        }

        lastClick.put(viewer.getUniqueId(), now);
        return false;
    }

    protected void playSound(Player viewer, Sound sound, float pitch) {
        viewer.playSound(viewer.getLocation(), sound, 1F, pitch);
    }

    private final class InteractListener extends PacketListenerAbstract {

        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) {
                return;
            }

            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return;
            }

            Player viewer = event.getPlayer();
            if (viewer == null) {
                return;
            }

            int entityId = packet.getEntityId();
            Bukkit.getScheduler().runTask(getReplay().getPlugin(), () -> watch(viewer, entityId));
        }

    }

    private record OpenMenu(Inventory inventory, List<UUID> targets) {
    }

}

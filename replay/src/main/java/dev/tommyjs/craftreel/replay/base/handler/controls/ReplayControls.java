package dev.tommyjs.craftreel.replay.base.handler.controls;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

public final class ReplayControls {

    public static final int HELD_SLOT = 4;
    public static final long[] SKIP_INTERVALS = {5 * 20L, 20 * 20L, 60 * 20L, 5 * 60 * 20L, 20 * 60 * 20L};

    private static final String REWIND_TEXTURE = "bb84f3a8704eb9256bb392993db56730b79f75ff029c2e35871fdbf3234fb90";
    private static final String FORWARD_TEXTURE = "c86185b1d519ade585f184c34f3f3e20bb641deb879e81378e4eaf209287";
    private static final String SLOW_TEXTURE = "e378cd1cdb81acf818177028bbf3a0780d5c9942d334099adab34581f90bb8ea";
    private static final String FAST_TEXTURE = "46e78ab322e100f01283999359ba13fbe277ed14bd1abcd5a9e6cf144d6ad6ce";

    private ReplayControls() {
    }

    public static @Nullable ReplayControl fromSlot(int slot) {
        for (ReplayControl control : ReplayControl.values()) {
            if (control.slot() == slot) {
                return control;
            }
        }
        return null;
    }

    public static boolean isControl(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        return name.startsWith("Rewind") || name.startsWith("Forward") || name.equals("Speed")
            || name.equals("Play") || name.equals("Pause");
    }

    public static ItemStack rewind(long interval) {
        return skull(REWIND_TEXTURE, ChatColor.AQUA + "Rewind (" + intervalLabel(interval) + ")",
            ChatColor.GRAY + "Right-click to jump back " + intervalLabel(interval),
            ChatColor.GRAY + "Left-click to change interval");
    }

    public static ItemStack forward(long interval) {
        return skull(FORWARD_TEXTURE, ChatColor.AQUA + "Forward (" + intervalLabel(interval) + ")",
            ChatColor.GRAY + "Right-click to jump ahead " + intervalLabel(interval),
            ChatColor.GRAY + "Left-click to change interval");
    }

    public static ItemStack slower(double speed) {
        return skull(SLOW_TEXTURE, ChatColor.AQUA + "Speed",
            ChatColor.GRAY + "Current speed: " + ChatColor.WHITE + speedLabel(speed),
            ChatColor.GRAY + "Click to slow down");
    }

    public static ItemStack faster(double speed) {
        return skull(FAST_TEXTURE, ChatColor.AQUA + "Speed",
            ChatColor.GRAY + "Current speed: " + ChatColor.WHITE + speedLabel(speed),
            ChatColor.GRAY + "Click to speed up");
    }

    public static ItemStack playPause(boolean paused) {

        ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) (paused ? 8 : 10));
        String name = paused ? ChatColor.AQUA + "Play" : ChatColor.AQUA + "Pause";
        return named(item, name, ChatColor.GRAY + (paused ? "Click to play" : "Click to pause"));
    }

    public static String speedLabel(double speed) {
        if (speed == Math.floor(speed)) {
            return "x" + (long) speed;
        }
        return "x" + speed;
    }

    public static String intervalLabel(long frames) {
        long seconds = frames / 20L;
        return seconds < 60 ? seconds + "s" : (seconds / 60) + "m";
    }

    private static ItemStack skull(String texture, String name, String... lore) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/" + texture + "\"}}}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes());
        profile.getProperties().put("textures", new Property("textures", encoded));

        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, profile);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to apply skull texture", e);
        }

        item.setItemMeta(meta);
        return named(item, name, lore);
    }

    private static ItemStack named(ItemStack item, String name, String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

}

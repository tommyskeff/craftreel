package dev.tommyjs.craftreel.record.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.EntityEquipment;
import dev.tommyjs.craftreel.protocol.entity.EntityMeta;
import dev.tommyjs.craftreel.protocol.entity.EntityMetadata;
import dev.tommyjs.craftreel.protocol.entity.EntityPose;
import dev.tommyjs.craftreel.protocol.entity.EntityPresence;
import dev.tommyjs.craftreel.protocol.entity.EntityAnimationType;
import dev.tommyjs.craftreel.protocol.entity.EntityEquipmentDelta;
import dev.tommyjs.craftreel.protocol.item.ItemStack;
import dev.tommyjs.craftreel.protocol.entity.EntityMetadataDelta;
import dev.tommyjs.craftreel.protocol.entity.EntityMetadataValue;
import dev.tommyjs.craftreel.protocol.entity.PlayerMeta;
import dev.tommyjs.craftreel.protocol.entity.EntityPotionEffect;
import dev.tommyjs.craftreel.protocol.entity.EntityPotionEffectState;
import dev.tommyjs.craftreel.protocol.entity.EntityVelocity;
import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.nms.NmsAccess;
import dev.tommyjs.reel.recorder.EntityRecorder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EntityCapture {

    private static final Map<org.bukkit.entity.EntityType, EntityType> LEGACY_TYPE_FALLBACK = buildFallback();

    private final MinecraftRecording recording;
    private final Entity entity;
    private final Identifier worldId;

    private static final double VELOCITY_EPSILON = 1.0e-3;
    private static final int POSE_KEYFRAME_INTERVAL = 100;

    private EntityRecorder recorder;
    private EntityPose lastPose;
    private final Map<Integer, EntityMetadataValue> lastMeta = new LinkedHashMap<>();
    private final Map<Integer, ItemStack> lastEquipment = new LinkedHashMap<>();
    private EntityPotionEffectState lastPotion;
    private final boolean tracksVelocity;
    private EntityVelocity lastVelocity;
    private int poseTick = 1;
    private boolean active;

    private static final ItemStack EMPTY = ItemStack.EMPTY;

    public EntityCapture(MinecraftRecording recording, Entity entity, Identifier worldId) {
        this.recording = recording;
        this.entity = entity;
        this.worldId = worldId;
        this.tracksVelocity = !(entity instanceof LivingEntity);
        this.active = snapshot();

        if (active) {
            recordInitialMetadata();
            recordInitialPotion();
            recordInitialVelocity();
        }
    }

    public void tick() {
        if (!active || recorder == null) {
            return;
        }

        EntityPose pose = getEntityPose(entity);
        if (poseTick++ % POSE_KEYFRAME_INTERVAL == 0) {
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_POSE, pose);
            lastPose = pose;
        } else if (!pose.equals(lastPose)) {
            recorder.recordDelta(CraftReelProtocol.Tracks.ENTITY_POSE, lastPose.to(pose));
            lastPose = pose;
        }

        Map<Integer, EntityMetadataValue> values = readMetadata();
        for (Map.Entry<Integer, EntityMetadataValue> entry : values.entrySet()) {
            int index = entry.getKey();
            EntityMetadataValue after = entry.getValue();
            EntityMetadataValue before = lastMeta.get(index);

            if (before == null) {
                lastMeta.put(index, after);
                continue;
            }

            if (!before.equals(after)) {
                recorder.recordDelta(CraftReelProtocol.Tracks.ENTITY_METADATA, new EntityMetadataDelta(index, before, after));
                lastMeta.put(index, after);
            }
        }

        EntityPotionEffectState potion = snapshotPotion();
        if (potion != null && !potion.equals(lastPotion)) {
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_POTION, potion);
            lastPotion = potion;
        }

        Map<Integer, ItemStack> equipment = currentEquipment();
        for (Map.Entry<Integer, ItemStack> entry : equipment.entrySet()) {
            int slot = entry.getKey();
            ItemStack after = entry.getValue();
            ItemStack before = lastEquipment.getOrDefault(slot, EMPTY);
            if (!before.equals(after)) {
                recorder.recordDelta(CraftReelProtocol.Tracks.ENTITY_EQUIPMENT, new EntityEquipmentDelta(slot, before, after));
                lastEquipment.put(slot, after);
            }
        }

        if (tracksVelocity) {
            EntityVelocity velocity = velocityOf(entity);
            if (changed(lastVelocity, velocity)) {
                recorder.recordEvent(CraftReelProtocol.Tracks.ENTITY_VELOCITY, velocity);
                lastVelocity = velocity;
            }
        }
    }

    public void onAnimation(EntityAnimationType type) {
        if (active && recorder != null) {
            recorder.recordEvent(CraftReelProtocol.Tracks.ENTITY_ANIMATION, type);
        }
    }

    public void markRemoved() {
        if (active && recorder != null) {
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_PRESENCE, new EntityPresence(false));
        }
    }

    public void stop() {
        recorder = null;
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    private boolean snapshot() {
        EntityPose pose = getEntityPose(entity);
        if (entity instanceof Player player) {
            recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.PLAYER);
            String[] skin = extractSkin(player);
            recorder.recordState(CraftReelProtocol.Tracks.PLAYER_META, new PlayerMeta(UUID.randomUUID(), worldId,
                extractProfileId(player), player.getName(), skin == null ? null : skin[0], skin == null ? null : skin[1]));
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_POSE, pose);
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_PRESENCE, new EntityPresence(true));
            recordEquipment();
            lastPose = pose;
            return true;
        }

        EntityType type = resolveType(entity);
        if (type == null) {
            return false;
        }

        recorder = recording.getRecorder().createEntity(CraftReelProtocol.Entities.ENTITY);
        recorder.recordState(CraftReelProtocol.Tracks.ENTITY_META, new EntityMeta(UUID.randomUUID(), worldId, type, objectDataOf(entity)));
        recorder.recordState(CraftReelProtocol.Tracks.ENTITY_POSE, pose);
        recorder.recordState(CraftReelProtocol.Tracks.ENTITY_PRESENCE, new EntityPresence(true));
        recordEquipment();
        lastPose = pose;
        return true;
    }

    private void recordInitialMetadata() {
        Map<Integer, EntityMetadataValue> values = readMetadata();
        if (!values.isEmpty()) {
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_METADATA, new EntityMetadata(values));
            lastMeta.putAll(values);
        }
    }

    private void recordInitialPotion() {
        EntityPotionEffectState potion = snapshotPotion();
        if (potion != null && !potion.effects().isEmpty()) {
            recorder.recordState(CraftReelProtocol.Tracks.ENTITY_POTION, potion);
            lastPotion = potion;
        }
    }

    private void recordInitialVelocity() {
        if (!tracksVelocity) {
            return;
        }
        EntityVelocity velocity = velocityOf(entity);
        if (changed(null, velocity)) {
            recorder.recordEvent(CraftReelProtocol.Tracks.ENTITY_VELOCITY, velocity);
            lastVelocity = velocity;
        }
    }

    private static Map<org.bukkit.entity.EntityType, EntityType> buildFallback() {
        Map<org.bukkit.entity.EntityType, EntityType> map = new EnumMap<>(org.bukkit.entity.EntityType.class);
        mapFallback(map, "SPLASH_POTION", "minecraft:potion");
        mapFallback(map, "EGG", "minecraft:egg");
        mapFallback(map, "FISHING_HOOK", "minecraft:fishing_bobber");
        return map;
    }

    private static void mapFallback(Map<org.bukkit.entity.EntityType, EntityType> map, String bukkit, String pe) {
        try {
            EntityType resolved = EntityTypes.getByName(pe);
            if (resolved != null) {
                map.put(org.bukkit.entity.EntityType.valueOf(bukkit), resolved);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private static EntityType resolveType(Entity entity) {
        try {
            EntityType type = SpigotConversionUtil.fromBukkitEntityType(entity.getType());
            if (type != null) {
                return type;
            }
        } catch (RuntimeException ignored) {
        }
        return LEGACY_TYPE_FALLBACK.get(entity.getType());
    }

    private static int objectDataOf(Entity entity) {
        if (entity instanceof ThrownPotion potion) {
            org.bukkit.inventory.ItemStack item = potion.getItem();
            return item == null ? 0 : item.getDurability();
        }
        return 0;
    }

    private static EntityVelocity velocityOf(Entity entity) {
        Vector v = entity.getVelocity();
        return new EntityVelocity(v.getX(), v.getY(), v.getZ());
    }

    private static boolean changed(EntityVelocity before, EntityVelocity after) {
        if (before == null) {
            return after.x() != 0 || after.y() != 0 || after.z() != 0;
        }
        double dx = after.x() - before.x();
        double dy = after.y() - before.y();
        double dz = after.z() - before.z();
        return dx * dx + dy * dy + dz * dz > VELOCITY_EPSILON * VELOCITY_EPSILON;
    }

    private EntityPotionEffectState snapshotPotion() {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        List<EntityPotionEffect> effects = new ArrayList<>();
        for (PotionEffect effect : living.getActivePotionEffects()) {
            effects.add(new EntityPotionEffect(effect.getType().getId(), effect.getAmplifier(),
                effect.getDuration(), effect.isAmbient(), effect.hasParticles()));
        }
        return new EntityPotionEffectState(effects);
    }

    private Map<Integer, EntityMetadataValue> readMetadata() {
        Map<Integer, EntityMetadataValue> out = new LinkedHashMap<>();
        try {
            for (Map.Entry<Integer, Object> entry : NmsAccess.readMetadata(entity).entrySet()) {
                EntityMetadataValue value = convert(entry.getValue());
                if (value != null) {
                    out.put(entry.getKey(), value);
                }
            }
        } catch (RuntimeException ignored) {
        }
        return out;
    }

    private static EntityMetadataValue convert(Object value) {
        if (value instanceof Byte b) {
            return EntityMetadataValue.ofByte(b);
        } else if (value instanceof Short s) {
            return EntityMetadataValue.ofShort(s);
        } else if (value instanceof Integer i) {
            return EntityMetadataValue.ofInt(i);
        } else if (value instanceof Float f) {
            return EntityMetadataValue.ofFloat(f);
        } else if (value instanceof String str) {
            return EntityMetadataValue.ofString(str);
        } else if (NmsAccess.isItemStack(value)) {
            org.bukkit.inventory.ItemStack bukkit = NmsAccess.toBukkitItem(value);
            ItemStack desc = bukkit == null ? null : convertItem(bukkit);
            return desc == null ? null : EntityMetadataValue.ofItem(desc);
        } else {
            return null;
        }
    }

    private void recordEquipment() {
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        Map<Integer, ItemStack> current = currentEquipment();
        Map<Integer, ItemStack> items = new LinkedHashMap<>();
        for (Map.Entry<Integer, ItemStack> entry : current.entrySet()) {
            if (!EMPTY.equals(entry.getValue())) {
                items.put(entry.getKey(), entry.getValue());
            }
        }
        recorder.recordState(CraftReelProtocol.Tracks.ENTITY_EQUIPMENT, new EntityEquipment(items));
        lastEquipment.putAll(current);
    }

    private Map<Integer, ItemStack> currentEquipment() {
        Map<Integer, ItemStack> items = new LinkedHashMap<>();
        if (!(entity instanceof LivingEntity living)) {
            return items;
        }
        org.bukkit.inventory.EntityEquipment eq = living.getEquipment();
        if (eq == null) {
            return items;
        }
        items.put(EntityEquipment.HELMET, convertItemOrEmpty(eq.getHelmet()));
        items.put(EntityEquipment.CHESTPLATE, convertItemOrEmpty(eq.getChestplate()));
        items.put(EntityEquipment.LEGGINGS, convertItemOrEmpty(eq.getLeggings()));
        items.put(EntityEquipment.BOOTS, convertItemOrEmpty(eq.getBoots()));
        items.put(EntityEquipment.MAIN_HAND, convertItemOrEmpty(eq.getItemInHand()));
        return items;
    }

    private static ItemStack convertItemOrEmpty(org.bukkit.inventory.ItemStack item) {
        ItemStack desc = convertItem(item);
        return desc == null ? EMPTY : desc;
    }

    private static ItemStack convertItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
            return null;
        }
        try {
            com.github.retrooper.packetevents.protocol.item.ItemStack peItem =
                SpigotConversionUtil.fromBukkitItemStack(item);
            int id = peItem.getType().getId(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
            return new ItemStack(id, item.getAmount(), item.getDurability(), peItem.getNBT());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static EntityPose getEntityPose(Entity entity) {
        Location l = entity.getLocation();
        return new EntityPose(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch(), l.getYaw());
    }

    private static UUID extractProfileId(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getProfile").invoke(handle);
            Object id = profile.getClass().getMethod("getId").invoke(profile);
            if (id instanceof UUID uuid) {
                return uuid;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return player.getUniqueId();
    }

    private static String[] extractSkin(Player player) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            Object profile = handle.getClass().getMethod("getProfile").invoke(handle);
            Object properties = profile.getClass().getMethod("getProperties").invoke(profile);
            Object textures = properties.getClass().getMethod("get", Object.class).invoke(properties, "textures");
            for (Object property : (Iterable<?>) textures) {
                String value = (String) property.getClass().getMethod("getValue").invoke(property);
                String signature = (String) property.getClass().getMethod("getSignature").invoke(property);
                return new String[]{value, signature};
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }

        return null;
    }

}

package dev.tommyjs.craftreel.record.entity;

import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.record.MinecraftRecording;
import dev.tommyjs.craftreel.record.event.EntityRecordEvent;
import dev.tommyjs.craftreel.record.nms.NmsAccess;
import dev.tommyjs.craftreel.record.nms.WorldAccessListener;
import dev.tommyjs.craftreel.record.world.ChunkBounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class EntityCaptureRegistry implements WorldAccessListener {

    private final MinecraftRecording recording;
    private final Identifier worldId;
    private final ChunkBounds bounds;
    private final Map<UUID, EntityCapture> captures = new LinkedHashMap<>();

    public EntityCaptureRegistry(MinecraftRecording recording, Identifier worldId, ChunkBounds bounds) {
        this.recording = recording;
        this.worldId = worldId;
        this.bounds = bounds;
    }

    public void add(Entity entity) {
        if (captures.containsKey(entity.getUniqueId())) {
            return;
        }
        Location loc = entity.getLocation();
        if (!bounds.contains(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
            return;
        }
        EntityRecordEvent event = new EntityRecordEvent(recording, entity);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        EntityCapture capture = new EntityCapture(recording, entity, worldId);
        if (capture.isActive()) {
            captures.put(entity.getUniqueId(), capture);
        }
    }

    @Override
    public void onEntityAdd(Object nmsEntity) {
        Entity entity = NmsAccess.getBukkitEntity(nmsEntity);
        if (entity != null) {
            add(entity);
        }
    }

    @Override
    public void onEntityRemove(Object nmsEntity) {
        Entity entity = NmsAccess.getBukkitEntity(nmsEntity);
        if (entity != null) {
            EntityCapture capture = captures.remove(entity.getUniqueId());
            if (capture != null) {
                capture.markRemoved();
                capture.stop();
            }
        }
    }

    public void tickAll() {
        for (EntityCapture capture : captures.values()) {
            capture.tick();
        }
    }

    public void stopAll() {
        for (EntityCapture capture : captures.values()) {
            capture.stop();
        }
        captures.clear();
    }

    public EntityCapture byEntity(Entity entity) {
        return captures.get(entity.getUniqueId());
    }

}

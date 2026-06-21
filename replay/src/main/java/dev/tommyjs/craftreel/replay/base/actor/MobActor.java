package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;

public class MobActor extends AbstractTrackedActor {

    public MobActor() {
        this(EntityPlaybackConfig.DEFAULT);
    }

    public MobActor(EntityPlaybackConfig config) {
        super(config);
    }

    @Override
    protected void registerCreate() {
        onCreate(CraftReelProtocol.Tracks.ENTITY_META, meta -> {
            EntityType type = meta.type();
            int objectData = meta.objectData();
            install(meta.id(), meta.worldId(), tracker ->
                EntityTypes.isTypeInstanceOf(type, EntityTypes.LIVINGENTITY)
                    ? tracker.spawnMob(type, ORIGIN)
                    : tracker.spawnObject(type, ORIGIN, objectData));
        });
    }

}

package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Direction;
import com.github.retrooper.packetevents.protocol.world.PaintingType;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.protocol.entity.HangingFacing;
import dev.tommyjs.craftreel.protocol.entity.ObjectData;
import dev.tommyjs.craftreel.protocol.entity.PaintingArt;
import dev.tommyjs.craftreel.util.Vec3;
import dev.tommyjs.dynworld.entity.EntityPose;

public class MobActor extends AbstractTrackedActor {

    private static final double BLOCK_CENTRE = 0.5;

    public MobActor() {
        this(EntityPlaybackConfig.DEFAULT);
    }

    public MobActor(EntityPlaybackConfig config) {
        super(config);
    }

    @Override
    protected void registerCreate() {
        onCreate(CraftReelProtocol.Tracks.ENTITY_META, meta -> {
            EntityType type = EntityTypes.getByName(meta.type());
            if (type == null) {
                return;
            }
            int objectData = meta.objectData();
            if (type == EntityTypes.PAINTING) {
                PaintingArt art = ObjectData.paintingArt(objectData);
                if (!ObjectData.hasHanging(objectData) || art == null) {
                    return;
                }
                HangingFacing facing = ObjectData.hangingFacing(objectData);
                PaintingType painting = PaintingType.getById(art.id());
                Direction direction = Direction.getByHorizontalIndex(facing.horizontalIndex());
                installOnFirstPose(meta.id(), meta.worldId(), (tracker, pose) -> tracker.spawnPainting(painting,
                    hangingBlock(pose, facing, art.widthPixels(), art.heightPixels()), direction));
                return;
            }
            if (type == EntityTypes.ITEM_FRAME && ObjectData.hasHanging(objectData)) {
                HangingFacing facing = ObjectData.hangingFacing(objectData);
                installOnFirstPose(meta.id(), meta.worldId(), (tracker, pose) -> tracker.spawnObject(recordedEntityId(), type,
                    hangingBlock(pose, facing, ObjectData.ITEM_FRAME_WIDTH_PIXELS, ObjectData.ITEM_FRAME_HEIGHT_PIXELS),
                    facing.horizontalIndex()));
                return;
            }
            install(meta.id(), meta.worldId(), tracker ->
                EntityTypes.isTypeInstanceOf(type, EntityTypes.LIVINGENTITY)
                    ? tracker.spawnMob(recordedEntityId(), type, ORIGIN)
                    : tracker.spawnObject(recordedEntityId(), type, ORIGIN, objectData));
        });
    }

    private static EntityPose hangingBlock(dev.tommyjs.craftreel.protocol.entity.EntityPose pose, HangingFacing facing,
                                           int widthPixels, int heightPixels) {
        Vec3 block = ObjectData.hangingBlock(pose.x(), pose.y(), pose.z(), facing, widthPixels, heightPixels);
        return EntityPose.of(block.x() + BLOCK_CENTRE, block.y() + BLOCK_CENTRE, block.z() + BLOCK_CENTRE, pose.yaw(), pose.pitch());
    }

}

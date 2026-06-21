package dev.tommyjs.craftreel.replay.base.actor;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import dev.tommyjs.craftreel.util.Identifier;
import dev.tommyjs.craftreel.protocol.CraftReelProtocol;
import dev.tommyjs.craftreel.replay.base.BaseResources;
import dev.tommyjs.craftreel.replay.reference.WorldContext;
import dev.tommyjs.craftreel.protocol.entity.EntityAnimationType;
import dev.tommyjs.craftreel.protocol.entity.EntityEquipment;
import dev.tommyjs.craftreel.protocol.entity.EntityMetadata;
import dev.tommyjs.craftreel.protocol.item.ItemStack;
import dev.tommyjs.craftreel.protocol.entity.EntityMetadataValue;
import dev.tommyjs.dynworld.entity.EntityPose;
import dev.tommyjs.dynworld.entity.EntityTracker;
import dev.tommyjs.dynworld.entity.PlayerEntity;
import dev.tommyjs.dynworld.entity.VirtualEntity;
import dev.tommyjs.reel.scene.AbstractActor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntSupplier;

public abstract class AbstractTrackedActor extends AbstractActor {

    protected static final EntityPose ORIGIN = EntityPose.of(0, 0, 0, 0, 0);

    private static final EquipmentSlot[] SLOTS = {
        EquipmentSlot.HELMET, EquipmentSlot.CHEST_PLATE, EquipmentSlot.LEGGINGS,
        EquipmentSlot.BOOTS, EquipmentSlot.MAIN_HAND, EquipmentSlot.OFF_HAND
    };

    protected final EntityPlaybackConfig config;

    private EntityTracker tracker;
    private VirtualEntity entity;
    private UUID entityKey;

    protected AbstractTrackedActor(@NotNull EntityPlaybackConfig config) {
        this.config = config;
    }

    protected abstract void registerCreate();

    @Override
    protected final void configure() {
        registerCreate();

        onState(CraftReelProtocol.Tracks.ENTITY_POSE, true, pose -> entity.move(toPose(pose), transition.isSingleForwardStep()));
        onState(CraftReelProtocol.Tracks.ENTITY_METADATA, true, state -> entity.setMetadata(toEntityData(state)));
        onState(CraftReelProtocol.Tracks.ENTITY_EQUIPMENT, true, state -> entity.setEquipment(toEquipment(state)));
        onState(CraftReelProtocol.Tracks.ENTITY_PRESENCE, true, state -> entity.setPresent(state.present()));
        onEvent(CraftReelProtocol.Tracks.ENTITY_VELOCITY, velocity -> entity.setVelocity(new Vector3d(velocity.x(), velocity.y(), velocity.z())));
        onEvent(CraftReelProtocol.Tracks.ENTITY_ANIMATION, this::animate);

        onDestroy(() -> {
            if (entityKey != null) {
                scene.getResourceManager().unpublish(BaseResources.ENTITY_ID, entityKey);
            }
            if (tracker != null && entity != null) {
                tracker.remove(entity);
                onEntityRemoved(entity);
            }
        });
    }

    protected void install(@NotNull UUID id, @NotNull Identifier worldId, @NotNull Function<EntityTracker, VirtualEntity> factory) {
        WorldContext context = scene.getResourceManager().require(BaseResources.WORLD, worldId);
        this.tracker = context.tracker();
        this.entity = factory.apply(tracker);
        this.entityKey = id;
        entity.setRange(config.renderDistance());
        if (entity instanceof PlayerEntity player) {
            player.setTablistLinger(config.tablistLinger());
        }
        scene.getResourceManager().publish(BaseResources.ENTITY_ID, id, (IntSupplier) entity::entityId);
        onEntitySpawned(entity);
    }

    protected void onEntitySpawned(@NotNull VirtualEntity entity) {
    }

    protected void onEntityRemoved(@NotNull VirtualEntity entity) {
    }

    private void animate(EntityAnimationType type) {
        switch (type) {
            case SWING -> entity.animate(
                WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);
            case HURT -> entity.animate(
                WrapperPlayServerEntityAnimation.EntityAnimationType.HURT);
            case CRITICAL -> entity.animate(
                WrapperPlayServerEntityAnimation.EntityAnimationType.CRITICAL_HIT);
            case DEATH -> entity.sendStatus((byte) 3);
        }
    }

    private static EntityPose toPose(dev.tommyjs.craftreel.protocol.entity.EntityPose pose) {
        return new EntityPose(pose.x(), pose.y(), pose.z(), pose.yaw(), pose.pitch(), pose.headYaw());
    }

    private static List<EntityData<?>> toEntityData(EntityMetadata state) {
        List<EntityData<?>> list = new ArrayList<>();
        for (Map.Entry<Integer, EntityMetadataValue> entry : state.values().entrySet()) {
            EntityMetadataValue value = entry.getValue();
            list.add(new EntityData(entry.getKey(), dataType(value.type()), dataValue(value)));
        }
        return list;
    }

    private static EntityDataType<?> dataType(EntityMetadataValue.Type type) {
        return switch (type) {
            case BYTE -> EntityDataTypes.BYTE;
            case SHORT -> EntityDataTypes.SHORT;
            case INT -> EntityDataTypes.INT;
            case FLOAT -> EntityDataTypes.FLOAT;
            case STRING -> EntityDataTypes.STRING;
            case BOOLEAN -> EntityDataTypes.BOOLEAN;
            case ITEMSTACK -> EntityDataTypes.ITEMSTACK;
        };
    }

    private static Object dataValue(EntityMetadataValue value) {
        if (value.type() == EntityMetadataValue.Type.ITEMSTACK) {
            return toItem((ItemStack) value.value());
        }
        return value.value();
    }

    private static List<Equipment> toEquipment(EntityEquipment state) {
        List<Equipment> list = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : state.items().entrySet()) {
            list.add(new Equipment(SLOTS[entry.getKey()], toItem(entry.getValue())));
        }
        return list;
    }

    private static com.github.retrooper.packetevents.protocol.item.ItemStack toItem(ItemStack item) {
        if (item.isEmpty()) {
            return com.github.retrooper.packetevents.protocol.item.ItemStack.EMPTY;
        }
        com.github.retrooper.packetevents.protocol.item.ItemStack.Builder builder =
            com.github.retrooper.packetevents.protocol.item.ItemStack.builder()
                .type(ItemTypes.getById(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion(), item.id()))
                .amount(item.amount())
                .legacyData(item.data());
        if (item.nbt() != null) {
            builder.nbt(item.nbt());
        }
        return builder.build();
    }

}

package dev.tommyjs.craftreel.record.nms;

import dev.tommyjs.dynworld.block.BlockState;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import static dev.tommyjs.craftreel.record.nms.NmsReflect.NMS_ENTITY;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.CRAFT_WORLD_GET_HANDLE;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.findConstructor;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.findMethod;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.forName;

public final class NmsAccess {

    private static final Class<?> NMS_WORLD = forName("net.minecraft.server.v1_8_R3.World");
    private static final Class<?> NMS_BLOCK_POSITION = NmsReflect.NMS_BLOCK_POSITION;
    private static final Class<?> NMS_IBLOCKDATA = forName("net.minecraft.server.v1_8_R3.IBlockData");
    private static final Class<?> NMS_BLOCK = forName("net.minecraft.server.v1_8_R3.Block");
    private static final Class<?> NMS_ENUM_PARTICLE = forName("net.minecraft.server.v1_8_R3.EnumParticle");
    private static final Class<?> NMS_ITEMSTACK = forName("net.minecraft.server.v1_8_R3.ItemStack");

    private static final Method CRAFT_ITEMSTACK_AS_BUKKIT = findMethod(
        forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack"), "asBukkitCopy", NMS_ITEMSTACK);

    private static final Method CRAFT_ENTITY_GET_HANDLE = findMethod(
        forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity"), "getHandle");

    private static final Method ENTITY_GET_ID = findMethod(NMS_ENTITY, "getId");
    private static final Method ENTITY_GET_BUKKIT = findMethod(NMS_ENTITY, "getBukkitEntity");
    private static final Method ENTITY_GET_DATA_WATCHER = findMethod(NMS_ENTITY, "getDataWatcher");

    private static final Constructor<?> BLOCK_POSITION_CTOR = findConstructor(NMS_BLOCK_POSITION,
        int.class, int.class, int.class);
    private static final Method WORLD_GET_TYPE = findMethod(NMS_WORLD, "getType", NMS_BLOCK_POSITION);
    private static final Method IBD_GET_BLOCK = findMethod(NMS_IBLOCKDATA, "getBlock");
    private static final Method BLOCK_GET_ID = findMethod(NMS_BLOCK, "getId", NMS_BLOCK);
    private static final Method BLOCK_TO_LEGACY = findMethod(NMS_BLOCK, "toLegacyData", NMS_IBLOCKDATA);

    private static final Method PARTICLE_VALUES = findMethod(NMS_ENUM_PARTICLE, "values");
    private static final Method PARTICLE_ID = findMethod(NMS_ENUM_PARTICLE, "c");
    private static final Method PARTICLE_NAME = findMethod(NMS_ENUM_PARTICLE, "b");

    private static Field dataWatcherMapField;
    private static Field watchableValueField;

    private NmsAccess() {
    }

    public static Entity getBukkitEntity(Object nmsEntity) {
        try {
            return (Entity) ENTITY_GET_BUKKIT.invoke(nmsEntity);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve Bukkit entity", e);
        }
    }

    public static BlockState readBlockState(World world, int x, int y, int z) {
        try {
            Object handle = CRAFT_WORLD_GET_HANDLE.invoke(world);
            Object position = BLOCK_POSITION_CTOR.newInstance(x, y, z);
            Object iblockdata = WORLD_GET_TYPE.invoke(handle, position);
            Object block = IBD_GET_BLOCK.invoke(iblockdata);
            int id = (int) BLOCK_GET_ID.invoke(null, block);
            int data = (int) BLOCK_TO_LEGACY.invoke(block, iblockdata);
            return BlockState.of(id, data);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read NMS block state", e);
        }
    }

    public static Map<Integer, Object> readMetadata(Entity entity) {
        try {
            Object handle = CRAFT_ENTITY_GET_HANDLE.invoke(entity);
            Object dataWatcher = ENTITY_GET_DATA_WATCHER.invoke(handle);
            Map<?, ?> rawMap = readDataWatcherMap(dataWatcher);
            Map<Integer, Object> out = new TreeMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                int index = ((Number) entry.getKey()).intValue();
                out.put(index, readWatchableValue(entry.getValue()));
            }
            return out;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to snapshot entity metadata", e);
        }
    }

    public static boolean isItemStack(Object value) {
        return NMS_ITEMSTACK.isInstance(value);
    }

    public static ItemStack toBukkitItem(Object nmsItemStack) {
        try {
            return (ItemStack) CRAFT_ITEMSTACK_AS_BUKKIT.invoke(null, nmsItemStack);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    public static String particleName(int particleId) {
        try {
            Object[] values = (Object[]) PARTICLE_VALUES.invoke(null);
            for (Object value : values) {
                if ((int) PARTICLE_ID.invoke(value) == particleId) {
                    return (String) PARTICLE_NAME.invoke(value);
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return String.valueOf(particleId);
    }

    private static Map<?, ?> readDataWatcherMap(Object dataWatcher) throws ReflectiveOperationException {
        Field field = dataWatcherMapField;
        if (field == null) {
            for (Field f : dataWatcher.getClass().getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType()) && !Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    field = f;
                    break;
                }
            }
            if (field == null) {
                throw new NoSuchFieldException("No Map field on " + dataWatcher.getClass().getName());
            }
            dataWatcherMapField = field;
        }
        return (Map<?, ?>) field.get(dataWatcher);
    }

    private static Object readWatchableValue(Object watchable) throws ReflectiveOperationException {
        Field field = watchableValueField;
        if (field == null) {
            for (Field f : watchable.getClass().getDeclaredFields()) {
                if (f.getType() == Object.class) {
                    f.setAccessible(true);
                    field = f;
                    break;
                }
            }
            if (field == null) {
                throw new NoSuchFieldException("No Object value field on " + watchable.getClass().getName());
            }
            watchableValueField = field;
        }
        return field.get(watchable);
    }

}

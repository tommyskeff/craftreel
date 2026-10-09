package dev.tommyjs.craftreel.replay.base;

import org.bukkit.World;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class BlockEntityAccess {

    private static final Class<?> NMS_WORLD = forName("net.minecraft.server.v1_8_R3.World");
    private static final Class<?> NMS_BLOCK_POSITION = forName("net.minecraft.server.v1_8_R3.BlockPosition");
    private static final Class<?> NMS_TILE_ENTITY = forName("net.minecraft.server.v1_8_R3.TileEntity");
    private static final Class<?> NMS_NBT_COMPOUND = forName("net.minecraft.server.v1_8_R3.NBTTagCompound");

    private static final Method CRAFT_WORLD_GET_HANDLE = findMethod(
        forName("org.bukkit.craftbukkit.v1_8_R3.CraftWorld"), "getHandle");
    private static final Constructor<?> BLOCK_POSITION_CTOR = findConstructor(NMS_BLOCK_POSITION,
        int.class, int.class, int.class);
    private static final Method WORLD_GET_TILE_ENTITY = findMethod(NMS_WORLD, "getTileEntity", NMS_BLOCK_POSITION);
    private static final Method WORLD_REMOVE_TILE_ENTITY = findMethod(NMS_WORLD, "t", NMS_BLOCK_POSITION);
    private static final Method WORLD_NOTIFY = findMethod(NMS_WORLD, "notify", NMS_BLOCK_POSITION);
    private static final Method TILE_ENTITY_LOAD = findMethod(NMS_TILE_ENTITY, "a", NMS_NBT_COMPOUND);
    private static final Method NBT_COMPOUND_SET_INT = findMethod(NMS_NBT_COMPOUND, "setInt", String.class, int.class);
    private static final Method NBT_READ = findMethod(
        forName("net.minecraft.server.v1_8_R3.NBTCompressedStreamTools"), "a", DataInputStream.class);

    private BlockEntityAccess() {
    }

    public static void load(World world, int x, int y, int z, byte[] nbt) {
        try {
            Object handle = CRAFT_WORLD_GET_HANDLE.invoke(world);
            Object position = BLOCK_POSITION_CTOR.newInstance(x, y, z);
            WORLD_REMOVE_TILE_ENTITY.invoke(handle, position);
            Object tileEntity = WORLD_GET_TILE_ENTITY.invoke(handle, position);
            if (tileEntity == null) {
                return;
            }

            Object tag = NBT_READ.invoke(null, new DataInputStream(new ByteArrayInputStream(nbt)));
            NBT_COMPOUND_SET_INT.invoke(tag, "x", x);
            NBT_COMPOUND_SET_INT.invoke(tag, "y", y);
            NBT_COMPOUND_SET_INT.invoke(tag, "z", z);
            TILE_ENTITY_LOAD.invoke(tileEntity, tag);
            WORLD_NOTIFY.invoke(handle, position);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to load NMS block entity", e);
        }
    }

    public static void remove(World world, int x, int y, int z) {
        try {
            Object handle = CRAFT_WORLD_GET_HANDLE.invoke(world);
            WORLD_REMOVE_TILE_ENTITY.invoke(handle, BLOCK_POSITION_CTOR.newInstance(x, y, z));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to remove NMS block entity", e);
        }
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing NMS method " + owner.getName() + "#" + name, e);
        }
    }

    private static Constructor<?> findConstructor(Class<?> owner, Class<?>... params) {
        try {
            Constructor<?> c = owner.getConstructor(params);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing NMS constructor " + owner.getName(), e);
        }
    }

    private static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not running on NMS/CraftBukkit v1_8_R3: " + name, e);
        }
    }

}

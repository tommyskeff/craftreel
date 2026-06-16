package dev.tommyjs.craftreel.record.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class NmsReflect {

    static final Class<?> NMS_BLOCK_POSITION = forName("net.minecraft.server.v1_8_R3.BlockPosition");
    static final Class<?> NMS_ENTITY = forName("net.minecraft.server.v1_8_R3.Entity");

    static final Method CRAFT_WORLD_GET_HANDLE = findMethod(
        forName("org.bukkit.craftbukkit.v1_8_R3.CraftWorld"), "getHandle");

    private NmsReflect() {
    }

    static Method findMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing NMS method " + owner.getName() + "#" + name, e);
        }
    }

    static Constructor<?> findConstructor(Class<?> owner, Class<?>... params) {
        try {
            Constructor<?> c = owner.getConstructor(params);
            c.setAccessible(true);
            return c;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing NMS constructor " + owner.getName(), e);
        }
    }

    static Class<?> forName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not running on NMS/CraftBukkit v1_8_R3: " + name, e);
        }
    }

}

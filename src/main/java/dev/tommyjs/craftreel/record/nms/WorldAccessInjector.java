package dev.tommyjs.craftreel.record.nms;

import org.bukkit.World;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.WeakHashMap;

import static dev.tommyjs.craftreel.record.nms.NmsReflect.CRAFT_WORLD_GET_HANDLE;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.findMethod;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.forName;

public final class WorldAccessInjector {

    private static final Class<?> NMS_IWORLDACCESS = forName("net.minecraft.server.v1_8_R3.IWorldAccess");
    private static final Method ADD_IWORLDACCESS = findMethod(
        forName("net.minecraft.server.v1_8_R3.World"), "addIWorldAccess", NMS_IWORLDACCESS);

    private static final WeakHashMap<Object, WorldAccessDispatcher> dispatchers = new WeakHashMap<>();

    private WorldAccessInjector() {
    }

    public static synchronized WorldAccessDispatcher inject(World world) {
        try {
            Object handle = CRAFT_WORLD_GET_HANDLE.invoke(world);
            WorldAccessDispatcher existing = dispatchers.get(handle);
            if (existing != null) {
                return existing;
            }

            WorldAccessDispatcher dispatcher = new WorldAccessDispatcher(NMS_IWORLDACCESS);
            Object proxy = Proxy.newProxyInstance(
                NMS_IWORLDACCESS.getClassLoader(),
                new Class[]{NMS_IWORLDACCESS},
                dispatcher);
            ADD_IWORLDACCESS.invoke(handle, proxy);
            dispatchers.put(handle, dispatcher);
            return dispatcher;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to inject IWorldAccess proxy", e);
        }
    }

}

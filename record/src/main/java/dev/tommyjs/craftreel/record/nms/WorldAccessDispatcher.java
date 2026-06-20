package dev.tommyjs.craftreel.record.nms;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dev.tommyjs.craftreel.record.nms.NmsReflect.NMS_BLOCK_POSITION;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.NMS_ENTITY;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.findMethod;
import static dev.tommyjs.craftreel.record.nms.NmsReflect.forName;

public final class WorldAccessDispatcher implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger("craftreel");
    public static volatile boolean DEBUG = false;

    private enum Semantic {
        BLOCK_CHANGE,
        SOUND,
        SOUND_PLAYER,
        PARTICLE,
        ENTITY_ADD,
        ENTITY_REMOVE,
        WORLD_EVENT,
        WORLD_EVENT_PLAYER,
        BLOCK_BREAK,
        IGNORE
    }

    private static final Class<?> NMS_ENTITY_HUMAN = forName("net.minecraft.server.v1_8_R3.EntityHuman");

    private static final Method BP_GET_X = findMethod(NMS_BLOCK_POSITION, "getX");
    private static final Method BP_GET_Y = findMethod(NMS_BLOCK_POSITION, "getY");
    private static final Method BP_GET_Z = findMethod(NMS_BLOCK_POSITION, "getZ");

    private final CopyOnWriteArrayList<WorldAccessListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<Method, Semantic> semantics = new HashMap<>();
    private volatile boolean loggedError;

    public WorldAccessDispatcher(Class<?> ifaceClass) {
        for (Method method : ifaceClass.getMethods()) {
            Semantic semantic = classify(method);
            semantics.put(method, semantic);
            if (DEBUG) {
                LOG.info("craftreel: IWorldAccess " + method.getName()
                    + Arrays.toString(method.getParameterTypes()) + " -> " + semantic);
            }
        }
    }

    public void addListener(WorldAccessListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WorldAccessListener listener) {
        listeners.remove(listener);
    }

    private Semantic classify(Method method) {
        String name = method.getName();
        Class<?>[] p = method.getParameterTypes();
        if (name.equals("a") && p.length == 1 && p[0] == NMS_BLOCK_POSITION) {
            return Semantic.BLOCK_CHANGE;
        }
        if (name.equals("b") && p.length == 1 && p[0] == NMS_BLOCK_POSITION) {
            return Semantic.IGNORE;
        }
        if (name.equals("a") && p.length == 6 && allInt(p)) {
            return Semantic.IGNORE;
        }
        if (name.equals("a") && p.length == 6 && p[0] == String.class) {
            return Semantic.SOUND;
        }
        if (name.equals("a") && p.length == 7 && p[0] == NMS_ENTITY_HUMAN && p[1] == String.class) {
            return Semantic.SOUND_PLAYER;
        }
        if (name.equals("a") && p.length == 9 && p[0] == int.class && p[1] == boolean.class) {
            return Semantic.PARTICLE;
        }
        if (name.equals("a") && p.length == 1 && p[0] == NMS_ENTITY) {
            return Semantic.ENTITY_ADD;
        }
        if (name.equals("b") && p.length == 1 && p[0] == NMS_ENTITY) {
            return Semantic.ENTITY_REMOVE;
        }
        if (name.equals("a") && p.length == 2 && p[0] == String.class && p[1] == NMS_BLOCK_POSITION) {
            return Semantic.IGNORE;
        }
        if (name.equals("a") && p.length == 3 && p[0] == int.class && p[1] == NMS_BLOCK_POSITION) {
            return Semantic.WORLD_EVENT;
        }
        if (name.equals("a") && p.length == 4 && p[0] == NMS_ENTITY_HUMAN && p[2] == NMS_BLOCK_POSITION) {
            return Semantic.WORLD_EVENT_PLAYER;
        }
        if (name.equals("b") && p.length == 3 && p[0] == int.class && p[1] == NMS_BLOCK_POSITION) {
            return Semantic.BLOCK_BREAK;
        }
        return Semantic.IGNORE;
    }

    private static boolean allInt(Class<?>[] p) {
        for (Class<?> c : p) {
            if (c != int.class) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (method.getDeclaringClass() == Object.class) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> "WorldAccessDispatcher@" + Integer.toHexString(System.identityHashCode(proxy));
                default -> null;
            };
        }

        Semantic semantic = semantics.get(method);
        if (semantic == null || semantic == Semantic.IGNORE) {
            return null;
        }

        if (DEBUG) {
            LOG.info("craftreel: dispatch " + semantic + " to " + listeners.size() + " listener(s)");
        }
        for (WorldAccessListener listener : listeners) {
            try {
                dispatch(semantic, listener, args);
            } catch (Throwable t) {
                if (!loggedError) {
                    loggedError = true;
                    LOG.log(Level.WARNING, "craftreel: IWorldAccess dispatch failed for " + semantic, t);
                }
            }
        }
        return null;
    }

    private void dispatch(Semantic semantic, WorldAccessListener listener, Object[] args) throws Throwable {
        switch (semantic) {
            case BLOCK_CHANGE: {
                int[] pos = pos(args[0]);
                listener.onBlockChange(pos[0], pos[1], pos[2]);
                break;
            }
            case SOUND:
                listener.onSound((String) args[0], (double) args[1], (double) args[2], (double) args[3],
                    (float) args[4], (float) args[5]);
                break;
            case SOUND_PLAYER:
                listener.onSound((String) args[1], (double) args[2], (double) args[3], (double) args[4],
                    (float) args[5], (float) args[6]);
                break;
            case PARTICLE:
                listener.onParticle((int) args[0], (boolean) args[1], (double) args[2], (double) args[3],
                    (double) args[4], (double) args[5], (double) args[6], (double) args[7], (int[]) args[8]);
                break;
            case ENTITY_ADD:
                listener.onEntityAdd(args[0]);
                break;
            case ENTITY_REMOVE:
                listener.onEntityRemove(args[0]);
                break;
            case WORLD_EVENT: {
                int[] pos = pos(args[1]);
                listener.onWorldEvent((int) args[0], pos[0], pos[1], pos[2], (int) args[2]);
                break;
            }
            case WORLD_EVENT_PLAYER: {
                int[] pos = pos(args[2]);
                listener.onWorldEvent((int) args[1], pos[0], pos[1], pos[2], (int) args[3]);
                break;
            }
            case BLOCK_BREAK: {
                int[] pos = pos(args[1]);
                listener.onBlockBreak((int) args[0], pos[0], pos[1], pos[2], (int) args[2]);
                break;
            }
            default:
                break;
        }
    }

    private static int[] pos(Object blockPosition) throws Throwable {
        return new int[]{
            (int) BP_GET_X.invoke(blockPosition),
            (int) BP_GET_Y.invoke(blockPosition),
            (int) BP_GET_Z.invoke(blockPosition)
        };
    }

}

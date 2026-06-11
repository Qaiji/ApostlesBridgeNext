package com.medua.apostlesbridgenext.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MinecraftReflectionUtil {
    private MinecraftReflectionUtil() { }

    public static Object createResourceId(String namespace, String path) {
        try {
            for (String className : new String[]{"net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation"}) {
                try {
                    Class<?> resourceClass = Class.forName(className);
                    return resourceClass.getMethod("fromNamespaceAndPath", String.class, String.class)
                        .invoke(null, namespace, path);
                } catch (ClassNotFoundException ignored) { }
            }
            throw new ClassNotFoundException("net.minecraft.resources.Identifier or ResourceLocation");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create resource id", exception);
        }
    }

    public static Object newInstance(String className, Object... args) {
        try {
            Class<?> targetClass = Class.forName(className);
            for (Constructor<?> constructor : targetClass.getConstructors()) {
                if (constructor.getParameterCount() != args.length || !areCompatible(constructor.getParameterTypes(), args)) {
                    continue;
                }
                return constructor.newInstance(args);
            }
            throw new NoSuchMethodException(className);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to create " + className, exception);
        }
    }

    public static boolean invokeAny(Object target, String methodName, Class<?>[] preferredParameterTypes, Object... args) {
        return invokeAny(target, new String[]{methodName}, preferredParameterTypes, args);
    }

    public static boolean invokeAny(Object target, String[] methodNames, Class<?>[] preferredParameterTypes, Object... args) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName, preferredParameterTypes);
                method.invoke(target, args);
                return true;
            } catch (ReflectiveOperationException | RuntimeException ignored) { }
        }

        for (String methodName : methodNames) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) {
                    continue;
                }
                if (!areCompatible(method.getParameterTypes(), args)) {
                    continue;
                }
                try {
                    method.invoke(target, args);
                    return true;
                } catch (ReflectiveOperationException | RuntimeException ignored) { }
            }
        }
        return false;
    }

    private static boolean areCompatible(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] == null) {
                continue;
            }
            Class<?> parameterType = wrapPrimitive(parameterTypes[i]);
            if (!parameterType.isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}

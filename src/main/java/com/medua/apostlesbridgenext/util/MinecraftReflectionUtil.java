package com.medua.apostlesbridgenext.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MinecraftReflectionUtil {
    private MinecraftReflectionUtil() { }

    public static Object createResourceId(String namespace, String path) {
        ReflectiveOperationException lastException = null;
        for (String className : new String[]{"net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation", "net.minecraft.class_2960"}) {
            try {
                Class<?> resourceClass = Class.forName(className);
                for (String methodName : new String[]{"fromNamespaceAndPath", "method_60655"}) {
                    try {
                        return resourceClass.getMethod(methodName, String.class, String.class).invoke(null, namespace, path);
                    } catch (NoSuchMethodException exception) {
                        lastException = exception;
                    }
                }
            } catch (ReflectiveOperationException exception) {
                lastException = exception;
            }
        }
        throw new IllegalStateException("Unable to create resource id", lastException);
    }

    public static Object newInstance(String className, Object... args) {
        return newInstance(new String[]{className}, args);
    }

    public static Object newInstance(String[] classNames, Object... args) {
        ReflectiveOperationException lastException = null;
        for (String className : classNames) {
            try {
                return newInstance(Class.forName(className), args);
            } catch (ReflectiveOperationException exception) {
                lastException = exception;
            }
        }
        throw new IllegalStateException("Unable to create " + String.join(" or ", classNames), lastException);
    }

    private static Object newInstance(Class<?> targetClass, Object... args) throws ReflectiveOperationException {
        for (Constructor<?> constructor : targetClass.getConstructors()) {
            if (constructor.getParameterCount() != args.length || !areCompatible(constructor.getParameterTypes(), args)) {
                continue;
            }
            return constructor.newInstance(args);
        }
        throw new NoSuchMethodException(targetClass.getName());
    }

    public static Class<?> forName(String... classNames) {
        ClassNotFoundException lastException = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exception) {
                lastException = exception;
            }
        }
        throw new IllegalStateException("Unable to find class " + String.join(" or ", classNames), lastException);
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

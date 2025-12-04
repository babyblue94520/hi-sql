package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.support.field.FieldGetter;
import pers.clare.hisql.support.field.FieldReflector;
import pers.clare.hisql.support.field.FieldSetter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class ClassUtil {
    private static final Map<Class<?>, Method[]> methodsMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Method[]> declaredMethodsMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> declaredFieldsMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, FieldReflector[]> declaredFieldReflectorsMap = new ConcurrentHashMap<>();


    public static Method[] getMethods(Class<?> clazz) {
        return methodsMap.computeIfAbsent(clazz, Class::getMethods);
    }

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return declaredMethodsMap.computeIfAbsent(clazz, Class::getDeclaredMethods);
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        return declaredFieldsMap.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    public static FieldReflector[] getFieldReflectors(Class<?> clazz) {
        return declaredFieldReflectorsMap.computeIfAbsent(clazz, ClassUtil::scanFieldReflectors);
    }

    private static FieldReflector[] scanFieldReflectors(
            Class<?> clazz
    ) {
        if (isIgnore(clazz)) return new FieldReflector[0];
        Map<String, Method> getterMethodMap = new HashMap<>();
        Map<String, Method> setterMethodMap = new HashMap<>();
        scanGetAndSetMethod(clazz, getterMethodMap, setterMethodMap);

        Field[] fields = ClassUtil.getDeclaredFields(clazz);
        FieldReflector[] result = new FieldReflector[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            FieldGetter getter = buildFieldGetter(field, getterMethodMap);
            FieldSetter setter = buildFieldSetter(field, setterMethodMap);
            result[i] = new FieldReflector(field, TypeUtil.toWrapperClass(field.getType()), getter, setter);
        }
        return result;
    }

    private static FieldGetter buildFieldGetter(Field field, Map<String, Method> getterMethodMap) {
        Method getterMethod = getterMethodMap.get(field.getName());
        if (getterMethod == null) {
            return target -> {
                try {
                    if (target == null) return null;
                    return field.get(target);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            return target -> {
                try {
                    if (target == null) return null;
                    return getterMethod.invoke(target);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    private static FieldSetter buildFieldSetter(Field field, Map<String, Method> setterMethodMap) {
        Method setterMethod = setterMethodMap.get(field.getName());
        if (setterMethod == null) {
            return (target, value) -> {
                try {
                    if (target == null) return;
                    field.set(target, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } else {
            return (target, value) -> {
                try {
                    if (target == null) return;
                    setterMethod.invoke(target, value);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        }

    }

    private static void scanGetAndSetMethod(
            Class<?> clazz
            , Map<String, Method> getterMethodMap
            , Map<String, Method> setMethodMap
    ) {
        Method[] methods = ClassUtil.getDeclaredMethods(clazz);
        for (Method method : methods) {
            int modifier = method.getModifiers();
            if (Modifier.isStatic(modifier) || !Modifier.isPublic(modifier)) continue;
            if (isGetMethod(method)) {
                getterMethodMap.put(getFieldName(method), method);
            } else if (isSetMethod(method)) {
                setMethodMap.put(getFieldName(method), method);
            }
        }
    }

    private static String getFieldName(Method method) {
        String methodName = method.getName();
        String name = null;
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            name = methodName.substring(2);
        }
        if (name == null || name.isEmpty()) return name;
        if (name.length() == 1) return name.toLowerCase();
        char c = name.charAt(0);
        char c2 = name.charAt(1);
        if (Character.isUpperCase(c) && Character.isUpperCase(c2)) {
            return name;
        } else {
            return Character.toLowerCase(c) + name.substring(1);
        }
    }

    private static boolean isGetMethod(Method method) {
        if (method.getParameterCount() != 0) return false;
        String methodName = method.getName();
        if (methodName.length() > 3 && methodName.startsWith("get")) {
            return true;
        }
        if (methodName.length() > 2 && methodName.startsWith("is")
            && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)
        ) {
            return true;
        }
        return false;
    }

    private static boolean isSetMethod(Method method) {
        if (method.getParameterCount() != 1) return false;
        String methodName = method.getName();
        if (methodName.length() > 3 && methodName.startsWith("set")) {
            return true;
        }
        return false;
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz == null
               || clazz.isPrimitive()
               || clazz.isArray()
               || clazz.isEnum()
               || clazz.isInterface()
               || clazz.isSynthetic()
               || Collection.class.isAssignableFrom(clazz)
               || clazz.getName().startsWith("java")
                ;
    }
}

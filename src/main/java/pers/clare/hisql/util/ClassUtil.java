package pers.clare.hisql.util;

import pers.clare.hisql.repository.SQLCrudRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class ClassUtil {

    public static boolean isBasicType(Class<?> type) {
        if (type == null) return false;
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

    public static Class<?> toClassType(Class<?> type) {
        if (type == null) return null;
        if (type.isPrimitive()) {
            if (type == byte.class) {
                return Byte.class;
            } else if (type == char.class) {
                return Byte.class;
            } else if (type == short.class) {
                return Short.class;
            } else if (type == int.class) {
                return Integer.class;
            } else if (type == long.class) {
                return Long.class;
            } else if (type == float.class) {
                return Float.class;
            } else if (type == double.class) {
                return Double.class;
            } else if (type == boolean.class) {
                return Boolean.class;
            }
        }
        return type;
    }

    public static Type[] findTypes(Class<?> clazz) {
        Map<Class<?>, Type[]> typesMap = new HashMap<>();
        Type[] types = findTypes(clazz, typesMap);
        if (types == null) {
            throw new IllegalArgumentException(String.format("%s entity class not found!", clazz));
        }
        for (Type type : types) {
            if (!(type instanceof Class)) {
                throw new IllegalArgumentException(String.format("%s %s class not found!", clazz, type));
            }
        }
        return types;
    }

    private static Type[] findTypes(Class<?> clazz, Map<Class<?>, Type[]> typesMap) {
        Type[] types = null;
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                typesMap.put((Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
                if (parameterizedType.getRawType() == SQLCrudRepository.class) {
                    types = parameterizedType.getActualTypeArguments();
                } else {
                    types = findTypes((Class<?>) parameterizedType.getRawType(), typesMap);
                }
                findTypeVariableToClass(types, clazz, typesMap);
            } else if (type instanceof Class) {
                types = findTypes((Class<?>) type, typesMap);
                findTypeVariableToClass(types, clazz, typesMap);
            }
        }
        return types;
    }

    private static void findTypeVariableToClass(Type[] types, Class<?> clazz, Map<Class<?>, Type[]> typesMap) {
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type instanceof TypeVariable) {
                for (int j = 0; j < clazz.getTypeParameters().length; j++) {
                    if (type.getTypeName().equals(clazz.getTypeParameters()[j].getTypeName())) {
                        types[i] = typesMap.get(clazz)[j];
                    }
                }
            }
        }
    }
}

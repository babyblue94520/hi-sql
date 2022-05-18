package pers.clare.hisql.util;

import org.springframework.lang.NonNull;
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

    /**
     * @return Returns Object if it is a Generic type
     */
    @NonNull
    public static Class<?> toClassType(Type type) {
        if (type instanceof Class) {
            return toClassType((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            Type result = ((ParameterizedType) type).getRawType();
            if (result instanceof Class) {
                return toClassType((Class<?>) result);
            }
        }
        return Object.class;
    }

    @NonNull
    public static Class<?> toClassType(@NonNull Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == byte.class) {
                return Byte.class;
            } else if (clazz == char.class) {
                return Byte.class;
            } else if (clazz == short.class) {
                return Short.class;
            } else if (clazz == int.class) {
                return Integer.class;
            } else if (clazz == long.class) {
                return Long.class;
            } else if (clazz == float.class) {
                return Float.class;
            } else if (clazz == double.class) {
                return Double.class;
            } else if (clazz == boolean.class) {
                return Boolean.class;
            }
        }
        return clazz;
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

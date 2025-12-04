package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.repository.SQLCrudRepository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class TypeUtil {

    public static boolean isBasicType(Class<?> type) {
        if (type == null) return false;
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

    public static boolean isBasicTypeArray(Class<?> type) {
        if (type.isArray()) {
            type = type.getComponentType();
            return isBasicType(type) || isBasicTypeArray(type);
        }
        return false;
    }

    public static Class<?> toWrapperClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            Type result = ((ParameterizedType) type).getRawType();
            if (result instanceof Class) {
                return toWrapperClass((Class<?>) result);
            }
        }
        return Object.class;
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

    public static Class<?> toWrapperClass(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) {
                return Boolean.class;
            } else if (clazz == byte.class) {
                return Byte.class;
            } else if (clazz == char.class) {
                return Character.class;
            } else if (clazz == double.class) {
                return Double.class;
            } else if (clazz == float.class) {
                return Float.class;
            } else if (clazz == int.class) {
                return Integer.class;
            } else if (clazz == long.class) {
                return Long.class;
            } else if (clazz == short.class) {
                return Short.class;
            } else {
                return clazz;
            }
        }
        return clazz;
    }

    public static Object getDefaultValue(Class<?> type, Object value) {
        if (value == null && type.isPrimitive()) {
            if (type == int.class) {
                return 0;
            } else if (type == boolean.class) {
                return false;
            } else if (type == byte.class) {
                return (byte) 0;
            } else if (type == short.class) {
                return (short) 0;
            } else if (type == long.class) {
                return 0L;
            } else if (type == float.class) {
                return 0.0f;
            } else if (type == double.class) {
                return 0.0d;
            } else if (type == char.class) {
                return '\u0000';
            }
        }
        return value;
    }

    public static Class<?> getValueClass(Type type, int index) {
        Type result = getValueType(type, index);
        if (result instanceof ParameterizedType) {
            return TypeUtil.toWrapperClass(((ParameterizedType) result).getRawType());
        } else {
            return TypeUtil.toWrapperClass(result);
        }
    }

    public static Type getValueType(Type type, int index) {
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments()[index];
        }
        return Object.class;
    }
}

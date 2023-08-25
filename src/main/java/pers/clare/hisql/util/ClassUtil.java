package pers.clare.hisql.util;

import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import pers.clare.hisql.repository.SQLCrudRepository;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class ClassUtil {

    private static final ConcurrentMap<Class<?>, Method[]> classDeclaredMethodsMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, Field[]> classDeclaredFieldsMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, Map<String, Field>> classNameFieldMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, List<Method>> classOrderMethodsMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Class<?>, List<Field>> classOrderFieldsMap = new ConcurrentHashMap<>();

    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return classDeclaredMethodsMap.computeIfAbsent(clazz, Class::getDeclaredMethods);
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        return classDeclaredFieldsMap.computeIfAbsent(clazz, Class::getDeclaredFields);
    }

    public static Map<String, Field> getNameFieldMap(Class<?> clazz) {
        return classNameFieldMap.computeIfAbsent(clazz, ClassUtil::toNameFieldMap);
    }

    private static Map<String, Field> toNameFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : getDeclaredFields(clazz)) {
            fieldMap.put(field.getName(), field);
        }
        return fieldMap;
    }

    public static List<Method> getOrderGetMethods(Class<?> clazz) {
        return classOrderMethodsMap.computeIfAbsent(clazz, ClassUtil::toOrderGetMethods);
    }

    private static List<Method> toOrderGetMethods(Class<?> clazz) {
        Map<String, Field> fieldMap = getNameFieldMap(clazz);
        return sort(ClassUtil.getDeclaredMethods(clazz), ClassUtil::isGetMethod, (o) -> {
            Order order = o.getAnnotation(Order.class);
            if (order == null) {
                Field field = fieldMap.get(methodToFieldName(o.getName()));
                if (field != null) {
                    order = field.getAnnotation(Order.class);
                }
            }
            return order;
        });
    }

    public static List<Field> getOrderFields(Class<?> clazz) {
        return classOrderFieldsMap.computeIfAbsent(clazz, ClassUtil::toOrderFields);
    }

    private static List<Field> toOrderFields(Class<?> clazz) {
        return sort(ClassUtil.getDeclaredFields(clazz), (f) -> true, (f) -> f.getAnnotation(Order.class));
    }

    private static <T> List<T> sort(T[] array, Function<T, Boolean> filter, Function<T, Order> getOrder) {
        List<OrderObject<T>> orderObjects = new ArrayList<>();
        List<T> others = new ArrayList<>();
        for (T o : array) {
            if (!filter.apply(o)) continue;
            Order order = getOrder.apply(o);
            if (order == null) {
                others.add(o);
            } else {
                orderObjects.add(new OrderObject<>(order.value(), o));
            }
        }
        orderObjects.sort(Comparator.comparingInt(a -> a.order));
        List<T> result = new ArrayList<>();
        for (OrderObject<T> orderObject : orderObjects) {
            result.add(orderObject.object);
        }
        result.addAll(others);
        return result;
    }

    public static boolean isGetMethod(Method method) {
        return Modifier.isPublic(method.getModifiers())
               && !Modifier.isStatic(method.getModifiers())
               && method.getParameters().length == 0
               && method.getName().startsWith("get");
    }


    public static String methodToFieldName(String name) {
        char[] cs = new char[name.length() - 3];
        name.getChars(3, name.length(), cs, 0);
        cs[0] = Character.toLowerCase(cs[0]);
        return new String(cs);
    }

    public static boolean isBasicType(Class<?> type) {
        if (type == null) return false;
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

    @NonNull
    public static Class<?> toClassType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
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

    static class OrderObject<T> {
        final int order;
        final T object;

        OrderObject(int order, T object) {
            this.order = order;
            this.object = object;
        }
    }
}

package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentGetHandler;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public class ArgumentGetHandlerUtil {

    public static ArgumentGetterResult build(Method method) {
        ArgumentGetterResult result = new ArgumentGetterResult();
        Parameter[] parameters = method.getParameters();
        int c = 0;
        for (Parameter p : parameters) {
            final int index = c++;
            buildArgumentGetter(result, p.getType(), p.getParameterizedType(), p.getName(), (arguments) -> arguments[index]);
        }
        return result;
    }

    public static void buildArgumentGetter(ArgumentGetterResult result, Class<?> clazz, Type type, String name, ArgumentGetHandler handler) {
        Class<?> componentType = clazz.getComponentType();
        if (clazz == Pagination.class) {
            result.pagination = handler;
        } else if (clazz == Sort.class) {
            result.sort = handler;
        } else if (clazz == ResultSetCallback.class) {
            result.resultSet = handler;
        } else if (clazz == PreparedStatementCallback.class) {
            result.preparedStatement = handler;
        } else if (clazz == ConnectionCallback.class) {
            result.connection = handler;
        } else if (clazz.isArray()) {
            if (ClassUtil.isBasicType(componentType)) {
                result.getters.put(name, handler);
            } else {
                if (componentType.isArray()) {
                    result.getters.put(name, handler);
                } else {
                    result.getters.put(name, buildArrayValueHandler(componentType, handler));
                }
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            if (type instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                Class<?> actualType = types.length > 0 ? (Class<?>) types[0] : null;
                if (actualType == null || ClassUtil.isBasicType(actualType)) {
                    result.getters.put(name, handler);
                } else {
                    if (actualType.isArray()) {
                        result.getters.put(name, handler);
                    } else {
                        result.getters.put(name, buildCollectionValueHandler(actualType, handler));
                    }
                }
            } else {
                result.getters.put(name, handler);
            }
        } else if (ClassUtil.isBasicType(clazz)) {
            result.getters.put(name, handler);
        } else {
            buildCustomTypeGetter(result, clazz, name, handler);
        }
    }

    private static void buildCustomTypeGetter(ArgumentGetterResult result, Class<?> clazz, String name, ArgumentGetHandler argumentGetHandler) {
        String fieldName;
        ArgumentGetHandler handler;
        for (Method method : clazz.getDeclaredMethods()) {
            if (notGetMethod(method)) continue;
            fieldName = toFieldName(method.getName());
            handler = (arguments) -> {
                try {
                    return method.invoke(argumentGetHandler.apply(arguments));
                } catch (Exception e) {
                    throw new HiSqlException(e);
                }
            };
            buildArgumentGetter(result, method.getReturnType(), method.getGenericReturnType(), name + '.' + fieldName, handler);
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !ClassUtil.isBasicType(superClazz)) {
            buildCustomTypeGetter(result, superClazz, name, argumentGetHandler);
        }
    }

    private static ArgumentGetHandler buildArrayValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
        List<Function<Object, Object>> functions = getFieldHandlers(clazz);
        return (arguments) -> {
            Object[] array = (Object[]) handler.apply(arguments);
            Object[][] result = new Object[array.length][];
            int i = 0;
            for (Object o : array) {
                result[i++] = getValues(o, functions);
            }
            return result;
        };
    }

    private static ArgumentGetHandler buildCollectionValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
        List<Function<Object, Object>> functions = getFieldHandlers(clazz);
        return (arguments) -> {
            Collection<Object> collection = (Collection<Object>) handler.apply(arguments);
            Object[][] result = new Object[collection.size()][];
            int i = 0;
            for (Object o : collection) {
                result[i++] = getValues(o, functions);
            }
            return result;
        };
    }


    private static List<Function<Object, Object>> getFieldHandlers(Class<?> clazz) {
        List<Function<Object, Object>> valueHandlers = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (notGetMethod(method)) continue;
            valueHandlers.add((target) -> {
                try {
                    return method.invoke(target);
                } catch (Exception e) {
                    throw new HiSqlException(e);
                }
            });
        }
        return valueHandlers;
    }

    private static Object[] getValues(Object target, List<Function<Object, Object>> functions) {
        Object[] values = new Object[functions.size()];
        for (int j = 0, l = functions.size(); j < l; j++) {
            values[j] = functions.get(j).apply(target);
        }
        return values;
    }

    private static boolean notGetMethod(Method method) {
        return !method.getName().startsWith("get")
                || method.getParameters().length > 0
                || Modifier.isStatic(method.getModifiers())
                || Modifier.isPrivate(method.getModifiers());
    }

    private static String toFieldName(String name) {
        if (name.startsWith("get") && name.length() > 3) {
            char[] cs = new char[name.length() - 3];
            name.getChars(3, name.length(), cs, 0);
            cs[0] = Character.toLowerCase(cs[0]);
            return new String(cs);
        } else {
            return name;
        }
    }

   public static class ArgumentGetterResult {
        private final Map<String, ArgumentGetHandler> getters = new HashMap<>();
        private ArgumentGetHandler pagination;
        private ArgumentGetHandler sort;
        private ArgumentGetHandler connection;
        private ArgumentGetHandler preparedStatement;
        private ArgumentGetHandler resultSet;

        public Map<String, ArgumentGetHandler> getGetters() {
            return getters;
        }

        public ArgumentGetHandler getPagination() {
            return pagination;
        }

        public ArgumentGetHandler getSort() {
            return sort;
        }

        public ArgumentGetHandler getConnection() {
            return connection;
        }

        public ArgumentGetHandler getPreparedStatement() {
            return preparedStatement;
        }

        public ArgumentGetHandler getResultSet() {
            return resultSet;
        }
    }
}



package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentHandler;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.support.SqlReplace;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

public class ArgumentParseUtil {

    public static ParseResult build(Method method) {
        ParseResult result = new ParseResult();
        Parameter[] parameters = method.getParameters();
        int c = 0;
        for (Parameter p : parameters) {
            final int index = c++;
            buildArgumentGetter(result, p.getType(), p.getParameterizedType(), p.getName(), (arguments) -> arguments[index]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static void buildArgumentGetter(ParseResult result, Class<?> clazz, Type type, String name, ArgumentHandler<?> handler) {
        if (SqlReplace.class.isAssignableFrom(clazz)) {
            result.getters.put(name, handler);
        } else if (clazz == Pagination.class) {
            result.pagination = (ArgumentHandler<Pagination>) handler;
        } else if (clazz == Sort.class) {
            result.sort = (ArgumentHandler<Sort>) handler;
        } else if (clazz == ConnectionCallback.class) {
            result.connection = (ArgumentHandler<ConnectionCallback<?>>) handler;
        } else if (clazz == PreparedStatementCallback.class) {
            result.preparedStatement = (ArgumentHandler<PreparedStatementCallback<?>>) handler;
        } else if (clazz == ResultSetCallback.class) {
            result.resultSet = (ArgumentHandler<ResultSetCallback<?>>) handler;
        } else if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
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

    private static void buildCustomTypeGetter(ParseResult result, Class<?> clazz, String name, ArgumentHandler<?> argumentHandler) {
        String fieldName;
        ArgumentHandler<?> handler;
        for (Method method : ClassUtil.getOrderGetMethods(clazz)) {
            fieldName = ClassUtil.methodToFieldName(method.getName());
            handler = (arguments) -> {
                try {
                    return method.invoke(argumentHandler.apply(arguments));
                } catch (Exception e) {
                    throw new HiSqlException(e);
                }
            };
            buildArgumentGetter(result, method.getReturnType(), method.getGenericReturnType(), fieldName, handler);
            buildArgumentGetter(result, method.getReturnType(), method.getGenericReturnType(), name + '.' + fieldName, handler);
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !ClassUtil.isBasicType(superClazz)) {
            buildCustomTypeGetter(result, superClazz, name, argumentHandler);
        }
    }

    private static ArgumentHandler<?> buildArrayValueHandler(Class<?> clazz, ArgumentHandler<?> handler) {
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

    @SuppressWarnings("unchecked")
    private static ArgumentHandler<?> buildCollectionValueHandler(Class<?> clazz, ArgumentHandler<?> handler) {
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
        for (Method method : ClassUtil.getOrderGetMethods(clazz)) {
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

    public static class ParseResult {
        private final Map<String, ArgumentHandler<?>> getters = new HashMap<>();
        private ArgumentHandler<Pagination> pagination;
        private ArgumentHandler<Sort> sort;
        private ArgumentHandler<ConnectionCallback<?>> connection;
        private ArgumentHandler<PreparedStatementCallback<?>> preparedStatement;
        private ArgumentHandler<ResultSetCallback<?>> resultSet;

        public Map<String, ArgumentHandler<?>> getGetters() {
            return getters;
        }

        public ArgumentHandler<Pagination> getPagination() {
            return pagination;
        }

        public ArgumentHandler<Sort> getSort() {
            return sort;
        }

        public ArgumentHandler<ConnectionCallback<?>> getConnection() {
            return connection;
        }

        public ArgumentHandler<PreparedStatementCallback<?>> getPreparedStatement() {
            return preparedStatement;
        }

        public ArgumentHandler<ResultSetCallback<?>> getResultSet() {
            return resultSet;
        }

        public boolean hasCallback() {
            return connection != null || preparedStatement != null || resultSet != null;
        }
    }
}



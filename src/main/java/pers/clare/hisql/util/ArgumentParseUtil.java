package pers.clare.hisql.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentHandler;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.support.SqlReplace;
import pers.clare.hisql.support.SqlReplacer;
import pers.clare.hisql.support.field.FieldGetter;
import pers.clare.hisql.support.field.FieldReflector;

import java.lang.reflect.*;
import java.util.*;

@UtilityClass
public class ArgumentParseUtil {

    public static ParseResult build(Method method) {
        ParseResult result = new ParseResult();
        Parameter[] parameters = method.getParameters();
        int c = 0;
        for (Parameter p : parameters) {
            final int index = c++;
            buildArgumentGetter(result, p.getType(), p.getParameterizedType(), p.getName(), arguments -> arguments[index]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static void buildArgumentGetter(ParseResult result, Class<?> clazz, Type type, String name, ArgumentHandler<?> handler) {
        if (SqlReplace.class.isAssignableFrom(clazz)) {
            result.getters.put(name, handler);
        } else if (clazz == SqlReplacer.class) {
            result.sqlReplacers.add((ArgumentHandler<SqlReplacer>) handler);
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
            if (TypeUtil.isBasicType(componentType)) {
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
                if (actualType == null || TypeUtil.isBasicType(actualType)) {
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
        } else if (TypeUtil.isBasicType(clazz)) {
            result.getters.put(name, handler);
        } else {
            buildCustomTypeGetter(result, clazz, name, handler);
        }
    }

    private static void buildCustomTypeGetter(ParseResult result, Class<?> clazz, String name, ArgumentHandler<?> argumentHandler) {
        String fieldName;
        ArgumentHandler<?> handler;
        for (FieldReflector reflector : ClassUtil.getFieldReflectors(clazz)) {
            Field field = reflector.getField();
            fieldName = field.getName();
            FieldGetter getter = reflector.getGetter();
            handler = arguments -> {
                try {
                    return getter.apply(argumentHandler.apply(arguments));
                } catch (Exception e) {
                    throw new HiSqlException(e);
                }
            };
            buildArgumentGetter(result, field.getType(), field.getGenericType(), fieldName, handler);
            buildArgumentGetter(result, field.getType(), field.getGenericType(), name + '.' + fieldName, handler);
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !TypeUtil.isBasicType(superClazz)) {
            buildCustomTypeGetter(result, superClazz, name, argumentHandler);
        }
    }

    private static ArgumentHandler<?> buildArrayValueHandler(Class<?> clazz, ArgumentHandler<?> handler) {
        FieldReflector[] fieldReflectors = ClassUtil.getFieldReflectors(clazz);
        return arguments -> {
            Object[] array = (Object[]) handler.apply(arguments);
            Object[][] result = new Object[array.length][];
            int i = 0;
            for (Object o : array) {
                result[i++] = getValues(o, fieldReflectors);
            }
            return result;
        };
    }

    @SuppressWarnings("unchecked")
    private static ArgumentHandler<?> buildCollectionValueHandler(Class<?> clazz, ArgumentHandler<?> handler) {
        FieldReflector[] fieldReflectors = ClassUtil.getFieldReflectors(clazz);
        return arguments -> {
            Collection<Object> collection = (Collection<Object>) handler.apply(arguments);
            Object[][] result = new Object[collection.size()][];
            int i = 0;
            for (Object o : collection) {
                result[i++] = getValues(o, fieldReflectors);
            }
            return result;
        };
    }

    private static Object[] getValues(Object target, FieldReflector[] fieldReflectors) {
        Object[] values = new Object[fieldReflectors.length];
        for (int j = 0, l = fieldReflectors.length; j < l; j++) {
            values[j] = fieldReflectors[j].getGetter().apply(target);
        }
        return values;
    }

    @Getter
    public static class ParseResult {
        private final Map<String, ArgumentHandler<?>> getters = new HashMap<>();
        private final List<ArgumentHandler<SqlReplacer>> sqlReplacers = new ArrayList<>();
        private ArgumentHandler<Pagination> pagination;
        private ArgumentHandler<Sort> sort;
        private ArgumentHandler<ConnectionCallback<?>> connection;
        private ArgumentHandler<PreparedStatementCallback<?>> preparedStatement;
        private ArgumentHandler<ResultSetCallback<?>> resultSet;

        public boolean hasCallback() {
            return connection != null || preparedStatement != null || resultSet != null;
        }
    }
}



package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentGetHandler;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplace;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.service.SQLStoreService;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

public abstract class SQLMethod implements MethodInterceptor {
    protected static final Object[] emptyArguments = new Object[0];
    protected final Class<?> returnType;
    protected SQLStoreService sqlStoreService;
    protected String sql;
    protected boolean readonly;
    protected SQLQueryReplaceBuilder sqlQueryReplaceBuilder;
    protected SQLQueryBuilder sqlQueryBuilder;
    protected Map<String, ArgumentGetHandler> replaces;
    protected Map<String, ArgumentGetHandler> valueHandlers;
    protected Method method;
    protected ArgumentGetHandler paginationHandler;
    protected ArgumentGetHandler sortHandler;
    protected ArgumentGetHandler resultSetCallback;

    protected SQLMethod(Class<?> returnType) {
        this.returnType = returnType;
    }

    public void init() {
        char[] cs = sql.toCharArray();
        if (SQLQueryReplaceBuilder.findKeyCount(cs) > 0) {
            sqlQueryReplaceBuilder = new SQLQueryReplaceBuilder(cs);
            this.replaces = new HashMap<>();
        } else if (SQLQueryBuilder.findKeyCount(cs) > 0) {
            sqlQueryBuilder = new SQLQueryBuilder(cs);
        }
        this.valueHandlers = new HashMap<>();
        int c = 0;
        Parameter[] parameters = method.getParameters();
        for (Parameter p : parameters) {
            int index = c++;
            buildArgumentValueHandler(p.getType(), p.getParameterizedType(), p.getName(), (arguments) -> arguments[index]);
        }
    }

    public void setSqlStoreService(SQLStoreService sqlStoreService) {
        this.sqlStoreService = sqlStoreService;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    protected Pagination getPagination(Object[] arguments) {
        if (paginationHandler == null) return null;
        return (Pagination) paginationHandler.apply(arguments);
    }

    protected Sort getSort(Object[] arguments) {
        if (sortHandler == null) return null;
        return (Sort) sortHandler.apply(arguments);
    }


    private void buildArgumentValueHandler(Class<?> clazz, Type type, String name, ArgumentGetHandler handler) {
        Class<?> componentType = clazz.getComponentType();
        if (clazz == Pagination.class) {
            paginationHandler = handler;
        } else if (clazz == Sort.class) {
            sortHandler = handler;
        } else if (clazz == ResultSetCallback.class) {
            resultSetCallback = handler;
        } else if (isSimpleType(clazz)) {
            setArgumentValueHandler(name, handler);
        } else if (clazz.isArray()) {
            if (isSimpleType(componentType)) {
                setArgumentValueHandler(name, handler);
            } else {
                if (componentType.isArray()) {
                    setArgumentValueHandler(name, handler);
                } else {
                    setArgumentValueHandler(name, buildArrayValueHandler(componentType, handler));
                }
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            if (type instanceof ParameterizedType) {
                Type[] types = ((ParameterizedType) type).getActualTypeArguments();
                Class<?> actualType = types.length > 0 ? (Class<?>) types[0] : null;
                if (actualType == null || isSimpleType(actualType)) {
                    setArgumentValueHandler(name, handler);
                } else {
                    if (actualType.isArray()) {
                        setArgumentValueHandler(name, handler);
                    } else {
                        setArgumentValueHandler(name, buildCollectionValueHandler(actualType, handler));
                    }
                }
            } else {
                setArgumentValueHandler(name, handler);
            }
        } else {
            buildReturnValueHandler(clazz, name, handler);
        }
    }

    private void buildReturnValueHandler(Class<?> clazz, String name, ArgumentGetHandler argumentGetHandler) {
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
            buildArgumentValueHandler(method.getReturnType(), method.getGenericReturnType(), name + '.' + fieldName, handler);
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !isSimpleType(superClazz)) {
            buildReturnValueHandler(superClazz, name, argumentGetHandler);
        }
    }

    private ArgumentGetHandler buildArrayValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
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

    private ArgumentGetHandler buildCollectionValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
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

    private Object[] getValues(Object target, List<Function<Object, Object>> functions) {
        Object[] values = new Object[functions.size()];
        for (int j = 0, l = functions.size(); j < l; j++) {
            values[j] = functions.get(j).apply(target);
        }
        return values;
    }

    private List<Function<Object, Object>> getFieldHandlers(Class<?> clazz) {
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


    private void setArgumentValueHandler(String name, ArgumentGetHandler handler) {
        if (replaces != null && sqlQueryReplaceBuilder.hasKey(name)) {
            replaces.put(name, handler);
        } else {
            valueHandlers.put(name, handler);
        }
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() || type.getName().startsWith("java.");
    }

    private boolean notGetMethod(Method method) {
        return !method.getName().startsWith("get")
                || method.getParameters().length > 0
                || Modifier.isStatic(method.getModifiers())
                || Modifier.isPrivate(method.getModifiers());
    }

    protected SQLQuery toSqlQuery(SQLQueryReplaceBuilder sqlQueryReplaceBuilder, Object[] arguments) {
        SQLQueryReplace replace = sqlQueryReplaceBuilder.build();
        Object value;
        for (Map.Entry<String, ArgumentGetHandler> entry : replaces.entrySet()) {
            value = entry.getValue().apply(arguments);
            if (value instanceof String) {
                replace.replace(entry.getKey(), (String) value);
            } else {
                throw new HiSqlException("%s must be String", entry.getKey());
            }
        }
        SQLQuery query = replace.buildQuery();
        for (Map.Entry<String, ArgumentGetHandler> entry : valueHandlers.entrySet()) {
            query.value(entry.getKey(), entry.getValue().apply(arguments));
        }
        return query;
    }

    protected SQLQuery toSqlQuery(SQLQueryBuilder sqlQueryBuilder, Object[] arguments) {
        SQLQuery query = sqlQueryBuilder.build();
        for (Map.Entry<String, ArgumentGetHandler> entry : valueHandlers.entrySet()) {
            query.value(entry.getKey(), entry.getValue().apply(arguments));
        }
        return query;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) {
        Object[] arguments = methodInvocation.getArguments();
        Pagination pagination = getPagination(arguments);
        Sort sort = getSort(arguments);
        SQLQuery query = null;
        String executeSQL = this.sql;
        if (sqlQueryReplaceBuilder != null) {
            query = toSqlQuery(sqlQueryReplaceBuilder, arguments);
        } else if (sqlQueryBuilder != null) {
            query = toSqlQuery(sqlQueryBuilder, arguments);
        }
        if (query == null) {
            if (pagination != null) {
                executeSQL = sqlStoreService.getContext().getPaginationMode().buildPaginationSQL(pagination, executeSQL);
            } else if (sort != null) {
                executeSQL = sqlStoreService.getContext().getPaginationMode().buildSortSQL(sort, executeSQL);
            }
        } else {
            StringBuilder sb = query.toSQL();
            if (pagination != null) {
                sqlStoreService.getContext().getPaginationMode().appendPaginationSQL(sb, pagination);
            } else if (sort != null) {
                sqlStoreService.getContext().getPaginationMode().appendSortSQL(sb, sort.getSorts());
            }
            executeSQL = sb.toString();
            arguments = emptyArguments;
        }
        if (resultSetCallback == null) {
            return doInvoke(executeSQL, arguments);
        } else {
            return sqlStoreService.query(readonly, executeSQL, (ResultSetCallback<?>) resultSetCallback.apply(methodInvocation.getArguments()), arguments);
        }
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

    abstract protected Object doInvoke(String sql, Object[] arguments);
}

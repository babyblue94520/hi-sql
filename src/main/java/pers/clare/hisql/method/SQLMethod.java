package pers.clare.hisql.method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import pers.clare.hisql.*;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentGetHandler;
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
    protected String sql;
    protected boolean readonly;
    protected HiSqlContext context;
    protected SQLStoreService sqlStoreService;
    protected SQLQueryReplaceBuilder sqlQueryReplaceBuilder;
    protected SQLQueryBuilder sqlQueryBuilder;
    protected Map<String, ArgumentGetHandler> replaces;
    protected Map<String, ArgumentGetHandler> valueHandlers;
    protected Method method;
    protected ArgumentGetHandler paginationHandler;
    protected ArgumentGetHandler sortHandler;

    public void init() {
        Parameter[] parameters = method.getParameters();
        char[] cs = sql.toCharArray();
        if (SQLQueryReplaceBuilder.findKeyCount(cs) > 0) {
            sqlQueryReplaceBuilder = new SQLQueryReplaceBuilder(cs);
            this.replaces = new HashMap<>();
        } else if (SQLQueryBuilder.findKeyCount(cs) > 0) {
            sqlQueryBuilder = new SQLQueryBuilder(cs);
        }
        this.valueHandlers = new HashMap<>();
        int c = 0;
        for (Parameter p : parameters) {
            buildArgumentValueHandler(p, c++);
        }
    }

    public void setContext(HiSqlContext context) {
        this.context = context;
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

    public void setSqlStoreService(SQLStoreService sqlStoreService) {
        this.sqlStoreService = sqlStoreService;
    }

    protected Pagination getPagination(Object[] arguments) {
        if (paginationHandler == null) return null;
        return (Pagination) paginationHandler.apply(arguments);
    }

    protected Sort getSort(Object[] arguments) {
        if (sortHandler == null) return null;
        return (Sort) sortHandler.apply(arguments);
    }

    private void buildArgumentValueHandler(Parameter p, int index) {
        Class<?> type = p.getType();
        ArgumentGetHandler handler = (arguments) -> arguments[index];
        if (isSimpleType(type)) {
            setArgumentValueHandler(p.getName(), handler);
        } else if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if (isSimpleType(componentType)) {
                setArgumentValueHandler(p.getName(), handler);
            } else {
                if (componentType.isArray()) {
                    setArgumentValueHandler(p.getName(), handler);
                } else {
                    setArgumentValueHandler(p.getName(), buildArrayArgumentValueHandler(componentType, handler));
                }
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            Type[] types = ((ParameterizedType) p.getParameterizedType()).getActualTypeArguments();
            Class<?> actualType = types.length > 0 ? (Class<?>) types[0] : null;
            if (actualType == null || isSimpleType(actualType)) {
                setArgumentValueHandler(p.getName(), handler);
            } else {
                if (actualType.isArray()) {
                    setArgumentValueHandler(p.getName(), handler);
                } else {
                    setArgumentValueHandler(p.getName(), buildCollectionArgumentValueHandler((Class<?>) types[0], handler));
                }
            }
        } else if (type == Pagination.class) {
            paginationHandler = handler;
        } else if (type == Sort.class) {
            sortHandler = handler;
        } else {
            buildArgumentValueHandler(type, p.getName(), handler);
        }
    }

    private void buildArgumentValueHandler(Class<?> clazz, String key, ArgumentGetHandler handler) {
        Field[] fields = clazz.getDeclaredFields();
        int modifier;
        Class<?> type;
        ArgumentGetHandler fieldHandler;
        for (Field field : fields) {
            modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) continue;
            type = field.getType();
            field.setAccessible(true);
            fieldHandler = (arguments) -> {
                try {
                    return field.get(handler.apply(arguments));
                } catch (Exception e) {
                    throw new HiSqlException(e);
                }
            };
            if (isSimpleType(type)) {
                setArgumentValueHandler(key + '.' + field.getName(), fieldHandler);
                setArgumentValueHandler(field.getName(), fieldHandler);
            } else if (type == Pagination.class) {
                paginationHandler = fieldHandler;
            } else if (type == Sort.class) {
                sortHandler = fieldHandler;
            } else {
                buildArgumentValueHandler(type, key + '.' + field.getName(), fieldHandler);
            }
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !isSimpleType(superClazz)) {
            buildArgumentValueHandler(superClazz, key, handler);
        }
    }

    private ArgumentGetHandler buildArrayArgumentValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
        List<Function<Object, Object>> functions = getFieldHandlers(clazz);
        return (arguments) -> {
            Object[] array = (Object[]) handler.apply(arguments);
            Object[][] result = new Object[array.length][];
            Object[] values;
            int i = 0, j;
            for (Object o : array) {
                values = new Object[functions.size()];
                j = 0;
                for (Function<Object, Object> valueHandler : functions) {
                    values[j++] = valueHandler.apply(o);
                }
                result[i++] = values;
            }
            return result;
        };
    }

    @SuppressWarnings("unchecked")
    private ArgumentGetHandler buildCollectionArgumentValueHandler(Class<?> clazz, ArgumentGetHandler handler) {
        List<Function<Object, Object>> functions = getFieldHandlers(clazz);
        return (arguments) -> {
            Collection<Object> collection = (Collection<Object>) handler.apply(arguments);
            Object[][] result = new Object[collection.size()][];
            Object[] values;
            int i = 0, j;
            for (Object o : collection) {
                values = new Object[functions.size()];
                j = 0;
                for (Function<Object, Object> valueHandler : functions) {
                    values[j++] = valueHandler.apply(o);
                }
                result[i++] = values;
            }
            return result;
        };
    }

    private List<Function<Object, Object>> getFieldHandlers(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        int modifier;
        List<Function<Object, Object>> valueHandlers = new ArrayList<>();
        for (Field field : fields) {
            modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) continue;
            field.setAccessible(true);
            valueHandlers.add((target) -> {
                try {
                    return field.get(target);
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
        return type.isPrimitive() || type.getName().startsWith("java.lang");
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
                executeSQL = context.getPaginationMode().buildPaginationSQL(pagination, executeSQL);
            } else if (sort != null) {
                executeSQL = context.getPaginationMode().buildSortSQL(sort, executeSQL);
            }
        } else {
            StringBuilder sb = query.toSQL();
            if (pagination != null) {
                context.getPaginationMode().appendPaginationSQL(sb, pagination);
            } else if (sort != null) {
                context.getPaginationMode().appendSortSQL(sb, sort.getSorts());
            }
            executeSQL = sb.toString();
            arguments = emptyArguments;
        }
        return doInvoke(executeSQL, arguments);
    }

    abstract protected Object doInvoke(String sql, Object[] arguments);
}

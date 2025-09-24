package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentHandler;
import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplace;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.support.SqlReplace;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SQLQueryUtil {
    private SQLQueryUtil() {
    }

    public static void appendValue(
            StringBuilder sb
            , Object value
    ) {
        if (value instanceof String) {
            sb.append('\'');
            char[] cs = ((String) value).toCharArray();
            for (char c : cs) {
                switch (c) {
                    case '\'':
                    case '\\':
                        sb.append(c);
                        break;
                }
                sb.append(c);
            }
            sb.append('\'');
        } else {
            sb.append(value);
        }
    }

    // gen (?,?,?,?) or ((?,?),(?,?))
    public static void appendInValue(
            StringBuilder sb
            , Object value
    ) {
        Class<?> valueClass = value.getClass();
        if (ClassUtil.isBasicTypeArray(valueClass)) {
            sb.append('(');
            if (value instanceof Object[]) {
                Object[] vs = (Object[]) value;
                notEmpty(vs.length);
                for (Object v : vs) appendInValue(sb, v);
            } else if (value instanceof int[]) {
                int[] vs = (int[]) value;
                notEmpty(vs.length);
                for (int v : vs) appendInValue(sb, v);
            } else if (value instanceof long[]) {
                long[] vs = (long[]) value;
                notEmpty(vs.length);
                for (long v : vs) appendInValue(sb, v);
            } else if (value instanceof char[]) {
                char[] vs = (char[]) value;
                notEmpty(vs.length);
                for (char v : vs) appendInValue(sb, v);
            }
            sb.deleteCharAt(sb.length() - 1).append(')');
        } else if (Collection.class.isAssignableFrom(valueClass)) {
            @SuppressWarnings("unchecked")
            Collection<Object> vs = (Collection<Object>) value;
            notEmpty(vs.size());
            sb.append('(');
            for (Object v : vs) appendInValue(sb, v);
            sb.deleteCharAt(sb.length() - 1).append(')');
        } else {
            SQLQueryUtil.appendValue(sb, value);
        }
        sb.append(',');
    }

    public static String setValue(SQLQueryBuilder sqlQueryBuilder, Field[] fields, Object[] parameters) {
        SQLQuery sqlQuery = sqlQueryBuilder.build();
        if (parameters == null || parameters.length == 0) return sqlQuery.toString();
        for (int i = 0; i < parameters.length; i++) {
            sqlQuery.value(fields[i].getName(), parameters[i]);
        }
        return sqlQuery.toString();
    }

    public static <T> String setValue(SQLQueryBuilder sqlQueryBuilder, Field[] fields, T entity) {
        try {
            SQLQuery sqlQuery = sqlQueryBuilder.build();
            for (Field f : fields) {
                sqlQuery.value(f.getName(), f.get(entity));
            }
            return sqlQuery.toString();
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public static SQLQuery to(
            SQLQueryReplaceBuilder sqlQueryReplaceBuilder
            , Object[] arguments
            , Map<String, ArgumentHandler<?>> valueHandlers
    ) {
        SQLQueryReplace replace = sqlQueryReplaceBuilder.build();
        Map<String, Object> values = new HashMap<>();
        Object value;
        for (String key : sqlQueryReplaceBuilder.getKeys()) {
            ArgumentHandler<?> handler = valueHandlers.get(key);
            if (handler == null) continue;
            value = handler.apply(arguments);
            if (value instanceof String) {
                replace.replace(key, (String) value);
            } else if (value instanceof SqlReplace) {
                replace.replace(key, ((SqlReplace<?>) value).getSql());
                values.put(key, ((SqlReplace<?>) value).getValue());
            } else {
                throw new HiSqlException("%s must be String", key);
            }
        }
        SQLQuery query = replace.buildQuery();
        for (Map.Entry<String, ArgumentHandler<?>> entry : valueHandlers.entrySet()) {
            query.value(entry.getKey(), entry.getValue().apply(arguments));
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            query.value(entry.getKey(), entry.getValue());
        }
        return query;
    }

    public static SQLQuery to(
            SQLQueryBuilder sqlQueryBuilder
            , Object[] arguments
            , Map<String, ArgumentHandler<?>> valueHandlers
    ) {
        SQLQuery query = sqlQueryBuilder.build();
        for (Map.Entry<String, ArgumentHandler<?>> entry : valueHandlers.entrySet()) {
            query.value(entry.getKey(), entry.getValue().apply(arguments));
        }
        return query;
    }

    private static void notEmpty(int length) {
        if (length == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
    }

}

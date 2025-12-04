package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ArgumentHandler;
import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.query.SQLQueryReplace;
import pers.clare.hisql.query.SQLQueryReplaceBuilder;
import pers.clare.hisql.store.SQLStoreColumn;
import pers.clare.hisql.support.SqlReplace;
import pers.clare.hisql.support.SqlReplacer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class SQLQueryUtil {
    public static final String NULL = "NULL";

    public static void append(
            StringBuilder sb
            , Object value
    ) {

        if (value == null || value == NULL) {
            sb.append(NULL);
        } else {
            Class<?> valueClass = value.getClass();
            if (valueClass.isArray() || Collection.class.isAssignableFrom(valueClass)) {
                SQLQueryUtil.appendInValue(sb, value);
                sb.deleteCharAt(sb.length() - 1);
            } else {
                SQLQueryUtil.appendValue(sb, value);
            }
        }
    }

    private static void appendValue(
            StringBuilder sb
            , Object value
    ) {
        if (value == null) {
            sb.append(NULL);
        } else if (value instanceof String) {
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
    private static void appendInValue(
            StringBuilder sb
            , Object value
    ) {
        if (value == null) {
            sb.append(NULL);
        } else {
            Class<?> valueClass = value.getClass();
            if (TypeUtil.isBasicTypeArray(valueClass)) {
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
        }

        sb.append(',');
    }

    public static String setValue(SQLQueryBuilder sqlQueryBuilder, SQLStoreColumn[] columns, Object[] parameters) {
        SQLQuery sqlQuery = sqlQueryBuilder.build();
        if (parameters == null || parameters.length == 0) return sqlQuery.toString();
        for (int i = 0; i < parameters.length; i++) {
            sqlQuery.value(columns[i].getName(), parameters[i]);
        }
        return sqlQuery.toString();
    }

    public static <T> String setValue(SQLQueryBuilder sqlQueryBuilder, SQLStoreColumn[] columns, T entity) {
        try {
            SQLQuery sqlQuery = sqlQueryBuilder.build();
            for (SQLStoreColumn f : columns) {
                sqlQuery.value(f.getName(), f.getValue(entity));
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
            , List<ArgumentHandler<SqlReplacer>> sqlReplacers
    ) {
        SQLQueryReplace replace = sqlQueryReplaceBuilder.build();
        Map<String, Object> values = new HashMap<>();
        for (String key : sqlQueryReplaceBuilder.getKeys()) {
            ArgumentHandler<?> handler = valueHandlers.get(key);
            if (handler == null) continue;
            Object value = handler.apply(arguments);
            if (value instanceof String) {
                replace.replace(key, (String) value);
            } else if (value instanceof SqlReplace) {
                replace.replace(key, ((SqlReplace<?>) value).getSql());
                values.put(key, ((SqlReplace<?>) value).getValue());
            }
        }

        for (ArgumentHandler<SqlReplacer> handler : sqlReplacers) {
            var sqlReplacer = handler.apply(arguments);
            if (sqlReplacer == null) continue;
            for (SqlReplacer.Rule rule : sqlReplacer.getRules()) {
                String valueName = rule.getValueName();
                ArgumentHandler<?> valueHandler = valueHandlers.get(valueName);
                if (valueHandler == null) continue;
                Object value = valueHandler.apply(arguments);
                replace.replace(rule.getReplaceName(), rule.getSql(value));
                values.put(valueName, value);
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

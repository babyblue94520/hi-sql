package pers.clare.hisql.util;

import pers.clare.hisql.constant.CommandType;
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
import java.util.regex.Pattern;

public class SQLQueryUtil {
    // check string is select sql
    private static final Pattern select = Pattern.compile("^[\\s\\n]?select", Pattern.CASE_INSENSITIVE);
    private static final Pattern insert = Pattern.compile("^[\\s\\n]?insert", Pattern.CASE_INSENSITIVE);

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
                        sb.append('\\');
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
        if (valueClass.isArray()) {
            sb.append('(');
            if (value instanceof Object[]) {
                Object[] vs = (Object[]) value;
                if (vs.length == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
                for (Object v : vs) appendInValue(sb, v);
            } else if (value instanceof int[]) {
                int[] vs = (int[]) value;
                if (vs.length == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
                for (int v : vs) appendInValue(sb, v);
            } else if (value instanceof long[]) {
                long[] vs = (long[]) value;
                if (vs.length == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
                for (long v : vs) appendInValue(sb, v);
            } else if (value instanceof char[]) {
                char[] vs = (char[]) value;
                if (vs.length == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
                for (char v : vs) appendInValue(sb, v);
            }
            sb.deleteCharAt(sb.length() - 1).append(')');
        } else if (Collection.class.isAssignableFrom(valueClass)) {
            @SuppressWarnings("unchecked")
            Collection<Object> vs = (Collection<Object>) value;
            if (vs.size() == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
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

    public static int getCommandType(String command) {
        if (select.matcher(command).find()) return CommandType.Select;
        if (insert.matcher(command).find()) return CommandType.Insert;
        return CommandType.Update;
    }


    public static SQLQuery toSqlQuery(
            SQLQueryReplaceBuilder sqlQueryReplaceBuilder
            , Object[] arguments
            , Map<String, ArgumentHandler<?>> replaces
            , Map<String, ArgumentHandler<?>> valueHandlers
    ) {
        SQLQueryReplace replace = sqlQueryReplaceBuilder.build();
        Map<String, Object> values = new HashMap<>();
        Object value;
        for (Map.Entry<String, ArgumentHandler<?>> entry : replaces.entrySet()) {
            value = entry.getValue().apply(arguments);
            if (value instanceof String) {
                replace.replace(entry.getKey(), (String) value);
            } else if (value instanceof SqlReplace) {
                replace.replace(entry.getKey(), ((SqlReplace<?>) value).getSql());
                values.put(entry.getKey(), ((SqlReplace<?>) value).getValue());
            } else {
                throw new HiSqlException("%s must be String", entry.getKey());
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

    public static SQLQuery toSqlQuery(
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


}

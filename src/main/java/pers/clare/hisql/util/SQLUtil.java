package pers.clare.hisql.util;

import pers.clare.hisql.query.SQLQuery;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.FieldSetHandler;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;


public class SQLUtil {

    SQLUtil() {
    }

    public static Statement executeInsert(
            Connection conn
            , String sql
            , Object... parameters
    ) throws SQLException {
        if (parameters.length == 0) {
            Statement statement = conn.createStatement();
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            return statement;
        } else {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (Object value : parameters) {
                ps.setObject(i++, value);
            }
            ps.executeUpdate();
            return ps;
        }
    }

    public static void appendValue(
            StringBuilder sb
            , Object value
    ) {
        if (value instanceof String) {
            sb.append('\'');
            char[] cs = ((String) value).toCharArray();
            for (char c : cs) {
                sb.append(c);
                if (c == '\'') sb.append('\'');
            }
            sb.append('\'');
        } else {
            sb.append(value);
        }
    }

    /**
     * appendIn
     * 依陣列數量，動態產生 (?,?,?,?) or ((?,?),(?,?))
     *
     * @param sb
     * @param value
     */
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
            Collection<Object> vs = (Collection<Object>) value;
            if (vs.size() == 0) throw new IllegalArgumentException("SQL WHERE IN doesn't empty value");
            sb.append('(');
            for (Object v : vs) appendInValue(sb, v);
            sb.deleteCharAt(sb.length() - 1).append(')');
        } else {
            SQLUtil.appendValue(sb, value);
        }
        sb.append(',');
    }

    public static int setValue(
            PreparedStatement ps
            , Object... parameters
    ) throws SQLException {
        int index = 1;
        if (parameters == null || parameters.length == 0) return index;
        for (Object value : parameters) {
            if (value instanceof Pagination || value instanceof Sort) continue;
            ps.setObject(index++, value);
        }
        return index;
    }

    public static String setValue(SQLQueryBuilder sqlQueryBuilder, Field[] fields, Object[] parameters) {
        SQLQuery sqlQuery = sqlQueryBuilder.build();
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

    public static <T> T toInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        if (rs.next()) {
            return buildInstance(sqlStore.getConstructor(), fields, rs);
        }
        return null;
    }

    public static <T> Set<T> toSetInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        Set<T> result = new HashSet<>();
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        while (rs.next()) {
            result.add(buildInstance(sqlStore.getConstructor(), fields, rs));
        }
        return result;
    }

    public static <T> List<T> toInstances(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        List<T> list = new ArrayList<>();
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        while (rs.next()) {
            list.add(buildInstance(sqlStore.getConstructor(), fields, rs));
        }
        return list;
    }

    private static <T> T buildInstance(Constructor<T> constructor, FieldSetHandler[] fields, ResultSet rs) throws Exception {
        T target = constructor.newInstance();
        int i = 1;
        for (FieldSetHandler field : fields) {
            if (field == null) continue;
            field.apply(target, rs, i++);
        }
        return target;
    }

    private static FieldSetHandler[] toFields(Map<String, FieldSetHandler> fieldMap, ResultSetMetaData metaData) throws Exception {
        int l = metaData.getColumnCount();
        FieldSetHandler[] fields = new FieldSetHandler[l];
        for (int i = 0; i < l; i++) {
            fields[i] = fieldMap.get(metaData.getColumnLabel(i + 1));
        }
        return fields;
    }

}

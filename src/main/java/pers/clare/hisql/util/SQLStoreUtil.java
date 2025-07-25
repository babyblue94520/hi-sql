package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLData;
import pers.clare.hisql.store.SQLStoreColumn;

import java.lang.reflect.Type;
import java.util.*;

@UtilityClass
public class SQLStoreUtil {

    private static final Set<Class<?>> parameterizedTypes = new HashSet<>();

    static {

        parameterizedTypes.add(Optional.class);
        parameterizedTypes.add(Page.class);
        parameterizedTypes.add(Next.class);
    }

    public static SQLData toInsertSQLData(SQLCrudStore<?> sqlStore, Object entity) throws Exception {
        SQLStoreColumn[] columns = sqlStore.getColumns();
        StringBuilder columnSql = new StringBuilder("INSERT INTO " + sqlStore.getTableName() + "(");
        StringBuilder valueSql = new StringBuilder("VALUES(");
        List<Object> values = new ArrayList<>();
        Object value;
        for (SQLStoreColumn column : columns) {
            if (!column.isInsertable()) continue;
            value = column.getValue(entity);
            if (value == null) {
                if (column.isAuto()) continue;
                if (column.isNotNullable()) continue;
            }
            columnSql.append(column.getName())
                    .append(',');
            valueSql.append('?')
                    .append(',');
            values.add(value);
        }
        valueSql.deleteCharAt(valueSql.length() - 1).append(')');
        columnSql.deleteCharAt(columnSql.length() - 1)
                .append(')')
                .append(valueSql);
        return new SQLData(columnSql.toString(), values.toArray());
    }

    public static SQLData toUpdateSQLData(SQLCrudStore<?> sqlStore, Object entity) throws Exception {
        SQLStoreColumn[] columns = sqlStore.getColumns();
        StringBuilder valueSql = new StringBuilder("UPDATE " + sqlStore.getTableName() + " SET ");
        StringBuilder whereSql = new StringBuilder(" WHERE ");
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                Object value = column.getValue(entity);
                whereValues.add(value);
                whereSql.append(column.getName())
                        .append('=')
                        .append('?')
                        .append(" AND ");
            } else {
                if (!column.isUpdatable()) continue;
                Object value = column.getValue(entity);
                if (value == null && column.isNotNullable()) continue;
                setValues.add(value);
                valueSql.append(column.getName())
                        .append('=')
                        .append('?')
                        .append(',');
            }
        }
        whereSql.delete(whereSql.length() - 5, whereSql.length() - 1);
        valueSql.deleteCharAt(valueSql.length() - 1)
                .append(whereSql);
        Object[] values = new Object[setValues.size() + whereValues.size()];
        int i = 0;
        for (Object value : setValues) {
            values[i++] = value;
        }
        for (Object value : whereValues) {
            values[i++] = value;
        }
        return new SQLData(valueSql.toString(), values);
    }

    public static String buildCount(String tableName) {
        return "SELECT COUNT(*) FROM " + tableName;
    }

    public static SQLQueryBuilder buildCountById(SQLStoreColumn[] columns, String tableName) {
        String sql = "SELECT COUNT(*) FROM " +
                     tableName +
                     buildWhereById(columns);
        return SQLQueryBuilder.create(sql);
    }

    public static String buildSelect(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" FROM ").append(tableName);
        return sql.toString();
    }

    public static SQLQueryBuilder getSelectById(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" FROM ")
                .append(tableName)
                .append(buildWhereById(columns));
        return SQLQueryBuilder.create(sql.toString());
    }

    public static SQLQueryBuilder getSelectByIds(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" FROM ")
                .append(tableName)
                .append(buildWhereByIds(columns));
        return SQLQueryBuilder.create(sql.toString());
    }


    public static String appendSelectColumns(SQLBasicService service, Type returnType, String command) {
        Class<?> returnClass = ClassUtil.toWrapperClass(returnType);
        if (
                parameterizedTypes.contains(returnClass)
                || returnClass.isArray()
                || Collection.class.isAssignableFrom(returnClass)
        ) {
            returnClass = ClassUtil.getValueClass(returnType, 0);
        }
        if (returnClass == Map.class) {
            return "SELECT * " + command;
        } else {
            if (SQLStoreUtil.isIgnore(returnClass)) {
                throw new HiSqlException("Select return type not support type. %s", returnClass);
            }
            SQLStoreColumn[] columns = SQLStoreColumnUtil.create(returnClass, service);
            StringBuilder sb = new StringBuilder("SELECT ");
            for (SQLStoreColumn column : columns) {
                sb.append(column.getName()).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(' ').append(command);
            return sb.toString();
        }
    }

    public static String buildDelete(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public static SQLQueryBuilder buildDeleteById(SQLStoreColumn[] columns, String tableName) {
        String sql = "DELETE FROM " +
                     tableName +
                     buildWhereById(columns);
        return SQLQueryBuilder.create(sql);
    }

    public static SQLQueryBuilder buildDeleteByIds(SQLStoreColumn[] columns, String tableName) {
        String sql = "DELETE FROM " +
                     tableName +
                     buildWhereByIds(columns);
        return SQLQueryBuilder.create(sql);
    }


    public static <T> String buildInsertSQL(SQLCrudStore<T> store, T entity) {
        try {
            SQLStoreColumn[] columns = store.getColumns();
            String tableName = store.getTableName();

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append("(");
            StringBuilder valueSql = new StringBuilder("VALUES(");
            for (SQLStoreColumn column : columns) {
                if (!column.isInsertable()) continue;
                Object value = column.getValue(entity);
                if (value == null && (column.isAuto() || column.isNotNullable())) continue;
                sql.append(column.getName()).append(',');

                SQLQueryUtil.appendValue(valueSql, value);
                valueSql.append(',');
            }
            valueSql.deleteCharAt(valueSql.length() - 1).append(')');
            sql.deleteCharAt(sql.length() - 1).append(')');

            return sql.append(valueSql).toString();
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public static <T> String buildUpdateSQL(SQLCrudStore<T> store, T entity) {
        try {
            SQLStoreColumn[] columns = store.getColumns();
            String tableName = store.getTableName();

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE ").append(tableName).append(" SET ");
            StringBuilder whereSql = new StringBuilder(" WHERE ");
            String and = " AND ";
            for (SQLStoreColumn column : columns) {
                if (column.isId()) {
                    Object value = column.getValue(entity);
                    whereSql.append(column.getName())
                            .append('=');
                    SQLQueryUtil.appendValue(whereSql, value);
                    whereSql.append(and);
                } else {
                    if (!column.isUpdatable()) continue;
                    Object value = column.getValue(entity);
                    if (value == null && column.isNotNullable()) continue;

                    sql.append(column.getName())
                            .append('=');
                    SQLQueryUtil.appendValue(sql, value);
                    sql.append(',');
                }
            }
            whereSql.delete(whereSql.length() - and.length(), whereSql.length());
            sql.deleteCharAt(sql.length() - 1);
            return sql.append(whereSql).toString();
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    private static StringBuilder buildWhereById(SQLStoreColumn[] columns) {
        StringBuilder result = new StringBuilder(" WHERE ");
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                result.append(column.getName())
                        .append('=')
                        .append(':')
                        .append(column.getField().getName())
                        .append(" AND ");
            }
        }
        result.delete(result.length() - 5, result.length() - 1);
        return result;
    }

    private static StringBuilder buildWhereByIds(SQLStoreColumn[] columns) {
        StringBuilder result = new StringBuilder(" WHERE (");
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                result.append(column.getName()).append(',');
            }
        }
        result.delete(result.length() - 1, result.length());
        result.append(") IN :keys");
        return result;
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz == null
               || clazz.isPrimitive()
               || clazz.getName().startsWith("java")
               || clazz.isArray()
               || Collection.class.isAssignableFrom(clazz)
               || clazz.isEnum()
               || clazz.isInterface()
                ;
    }

    public static String normalizeWhitespace(String command) {
        char[] cs = command.toCharArray();
        char c;
        int count = 0;
        char[] temp = new char[cs.length];
        boolean pause = false;
        boolean space = false;
        for (int i = 0; i < cs.length; i++) {
            c = cs[i];
            switch (c) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    if (!pause) {
                        space = count > 0;
                        break;
                    }
                default:
                    if (space) {
                        temp[count++] = ' ';
                        space = false;
                    }
                    temp[count++] = c;
                    if (c == '\'') {
                        pause = !pause;
                    } else if (c == '\\' && cs[i + 1] == '\'') {
                        temp[count++] = '\'';
                        i++;
                    }
            }
        }
        return new String(temp, 0, count);
    }
}

package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLData;
import pers.clare.hisql.store.SQLStoreColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SQLStoreUtil {

    public static SQLData toInsertSQLData(SQLCrudStore<?> sqlStore, Object entity) throws Exception {
        SQLStoreColumn[] columns = sqlStore.getColumns();
        StringBuilder columnSql = new StringBuilder("insert into " + sqlStore.getTableName() + "(");
        StringBuilder valueSql = new StringBuilder("values(");
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
        StringBuilder valueSql = new StringBuilder("update " + sqlStore.getTableName() + " set ");
        StringBuilder whereSql = new StringBuilder(" where ");
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                Object value = column.getValue(entity);
                whereValues.add(value);
                whereSql.append(column.getName())
                        .append('=')
                        .append('?')
                        .append(" and ");
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

    public static SQLQueryBuilder buildCountById(SQLStoreColumn[] columns, String tableName) {
        String sql = "select count(*) from " +
                     tableName +
                     buildWhereById(columns);
        return SQLQueryBuilder.create(sql);
    }

    public static String buildSelect(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("select ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ").append(tableName);
        return sql.toString();
    }

    public static SQLQueryBuilder getSelectById(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("select ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ")
                .append(tableName)
                .append(buildWhereById(columns));
        return SQLQueryBuilder.create(sql.toString());
    }

    public static SQLQueryBuilder getSelectByIds(SQLStoreColumn[] columns, String tableName) {
        StringBuilder sql = new StringBuilder("select ");
        for (SQLStoreColumn column : columns) {
            sql.append(column.getName()).append(',');
        }
        sql.delete(sql.length() - 1, sql.length());
        sql.append(" from ")
                .append(tableName)
                .append(buildWhereByIds(columns));
        return SQLQueryBuilder.create(sql.toString());
    }

    public static SQLQueryBuilder buildDeleteById(SQLStoreColumn[] columns, String tableName) {
        String sql = "delete from " +
                     tableName +
                     buildWhereById(columns);
        return SQLQueryBuilder.create(sql);
    }

    public static SQLQueryBuilder buildDeleteByIds(SQLStoreColumn[] columns, String tableName) {
        String sql = "delete from " +
                     tableName +
                     buildWhereByIds(columns);
        return SQLQueryBuilder.create(sql);
    }


    public static <T> String buildInsertSQL(SQLCrudStore<T> store, T entity) {
        try {
            SQLStoreColumn[] columns = store.getColumns();
            String tableName = store.getTableName();

            StringBuilder sql = new StringBuilder();
            sql.append("insert into ").append(tableName).append("(");
            StringBuilder valueSql = new StringBuilder("values(");
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
            sql.append("update ").append(tableName).append(" set ");
            StringBuilder whereSql = new StringBuilder(" where ");
            String and = " and ";
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
        StringBuilder result = new StringBuilder(" where ");
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                result.append(column.getName())
                        .append('=')
                        .append(':')
                        .append(column.getField().getName())
                        .append(" and ");
            }
        }
        result.delete(result.length() - 5, result.length() - 1);
        return result;
    }

    private static StringBuilder buildWhereByIds(SQLStoreColumn[] columns) {
        StringBuilder result = new StringBuilder(" where (");
        for (SQLStoreColumn column : columns) {
            if (column.isId()) {
                result.append(column.getName()).append(',');
            }
        }
        result.delete(result.length() - 1, result.length());
        result.append(") in :keys");
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

}

package pers.clare.hisql.util;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.store.FieldColumn;
import pers.clare.hisql.store.SQLCrudStore;

import javax.persistence.Column;
import java.lang.reflect.Field;

public class SQLStoreUtil {

    public static SQLQueryBuilder buildCountById(FieldColumn[] fieldColumns, String tableName) {
        StringBuilder sql = new StringBuilder("select count(*) from ")
                .append(tableName)
                .append(" where ");
        String and = " and ";
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null) continue;
            if (fieldColumn.isId()) {
                sql.append(fieldColumn.getColumnName())
                        .append('=')
                        .append(':')
                        .append(fieldColumn.getField().getName())
                        .append(and);
            }
        }
        sql.delete(sql.length() - and.length(), sql.length() - 1);
        return SQLQueryBuilder.create(sql.toString());
    }


    public static String buildSelect(FieldColumn[] fieldColumns, String tableName) {
        StringBuilder sql = new StringBuilder("select ");
        if (fieldColumns != null && fieldColumns.length > 0) {
            for (FieldColumn column : fieldColumns) {
                sql.append(column.getColumnName()).append(',');
            }
            sql.delete(sql.length() - 1, sql.length());
        }
        sql.append(" from ").append(tableName);
        return sql.toString();
    }

    public static SQLQueryBuilder getSelectById(FieldColumn[] fieldColumns, String tableName) {
        StringBuilder sql = new StringBuilder("select ");
        StringBuilder columnSql = new StringBuilder();
        StringBuilder whereSql = new StringBuilder(" where ");
        String and = " and ";
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null) continue;
            columnSql.append(fieldColumn.getColumnName()).append(',');
            if (fieldColumn.isId()) {
                whereSql.append(fieldColumn.getColumnName())
                        .append('=')
                        .append(':')
                        .append(fieldColumn.getField().getName())
                        .append(and);
            }
        }
        columnSql.delete(columnSql.length() - 1, columnSql.length());
        whereSql.delete(whereSql.length() - and.length(), whereSql.length());
        sql.append(columnSql).append(" from ").append(tableName).append(whereSql);
        return SQLQueryBuilder.create(sql.toString());
    }

    public static SQLQueryBuilder buildDeleteById(FieldColumn[] fieldColumns, String tableName) {
        StringBuilder sql = new StringBuilder("delete from ")
                .append(tableName)
                .append(" where ");
        String and = " and ";
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null) continue;
            if (fieldColumn.isId()) {
                sql.append(fieldColumn.getColumnName())
                        .append('=')
                        .append(':')
                        .append(fieldColumn.getField().getName())
                        .append(and);
            }
        }
        sql.delete(sql.length() - and.length(), sql.length() - 1);

        return SQLQueryBuilder.create(sql.toString());
    }


    public static <T> String buildInsertSQL(SQLCrudStore<T> store, T entity) {
        try {
            FieldColumn[] fieldColumns = store.getFieldColumns();
            String tableName = store.getTableName();

            StringBuilder sql = new StringBuilder();
            sql.append("insert into ").append(tableName).append("(");
            StringBuilder valueSql = new StringBuilder("values(");
            for (FieldColumn fieldColumn : fieldColumns) {
                if (fieldColumn == null || !fieldColumn.isInsertable()) continue;
                Object value = fieldColumn.getField().get(entity);
                if (value == null && fieldColumn.isNotNullable()) continue;
                sql.append(fieldColumn.getColumnName()).append(',');

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
            FieldColumn[] fieldColumns = store.getFieldColumns();
            String tableName = store.getTableName();

            StringBuilder sql = new StringBuilder();
            sql.append("update ").append(tableName).append(" set ");
            StringBuilder whereSql = new StringBuilder(" where ");
            String and = " and ";
            for (FieldColumn fieldColumn : fieldColumns) {
                if (fieldColumn == null) continue;
                if (fieldColumn.isId()) {
                    Object value = fieldColumn.getField().get(entity);
                    whereSql.append(fieldColumn.getColumnName())
                            .append('=');
                    SQLQueryUtil.appendValue(whereSql, value);
                    whereSql.append(and);
                } else {
                    if (!fieldColumn.isUpdatable()) continue;
                    Object value = fieldColumn.getField().get(entity);
                    if (value == null && fieldColumn.isNotNullable()) continue;

                    sql.append(fieldColumn.getColumnName())
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


    public static String getColumnName(NamingStrategy namingStrategy, Field field, Column column) {
        return column == null || column.name().length() == 0 ? namingStrategy.turnCamelCase(field.getName()) : column.name();
    }

}

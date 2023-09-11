package pers.clare.hisql.util;

import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.store.FieldColumn;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLData;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SQLStoreUtil {

    public static SQLData toInsertSQLData(SQLCrudStore<?> sqlStore, Object entity) throws IllegalAccessException {
        FieldColumn[] fieldColumns = sqlStore.getFieldColumns();
        StringBuilder columnSql = new StringBuilder("insert into " + sqlStore.getTableName() + "(");
        StringBuilder valueSql = new StringBuilder("values(");
        List<Object> values = new ArrayList<>();
        Object value;
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null || !fieldColumn.isInsertable()) continue;
            value = fieldColumn.getField().get(entity);
            if (value == null) {
                if (fieldColumn.isAuto()) continue;
                if (fieldColumn.isNotNullable()) continue;
            }
            columnSql.append(fieldColumn.getColumnName())
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


    public static SQLData toUpdateSQLData(SQLCrudStore<?> sqlStore, Object entity) throws IllegalAccessException {
        FieldColumn[] fieldColumns = sqlStore.getFieldColumns();
        StringBuilder valueSql = new StringBuilder("update " + sqlStore.getTableName() + " set ");
        StringBuilder whereSql = new StringBuilder(" where ");
        List<Object> setValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null) continue;
            Object value = fieldColumn.getField().get(entity);
            if (fieldColumn.isId()) {
                whereSql.append(fieldColumn.getColumnName())
                        .append('=')
                        .append('?');
                whereValues.add(value);
                whereSql.append(" and ");
            } else {
                if (!fieldColumn.isUpdatable()) continue;
                if (value == null && fieldColumn.isNotNullable()) continue;

                valueSql.append(fieldColumn.getColumnName())
                        .append('=')
                        .append('?')
                        .append(',');
                setValues.add(value);
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


    public static String getColumnName(NamingStrategy namingStrategy, Field field, Column column) {
        return column == null || column.name().length() == 0 ? namingStrategy.turnCamelCase(field.getName()) : column.name();
    }
}

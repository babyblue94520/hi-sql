package pers.clare.hisql.store;


import pers.clare.hisql.function.FieldSetter;
import pers.clare.hisql.query.SQLQueryBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

public class SQLCrudStore<T> extends SQLStore<T> {
    private final String tableName;
    private final FieldColumn[] fieldColumns;
    private final Field autoKey;
    private final Field[] keyFields;
    private final String count;
    private final SQLQueryBuilder countById;
    private final String select;
    private final SQLQueryBuilder selectById;
    private final String deleteAll;
    private final SQLQueryBuilder deleteById;

    public SQLCrudStore(
            Constructor<T> constructor
            , Map<String, FieldSetter> fieldSetMap
            , String tableName
            , FieldColumn[] fieldColumns
            , Field autoKey
            , Field[] keyFields
            , String count
            , SQLQueryBuilder countById
            , String select
            , SQLQueryBuilder selectById
            , String deleteAll
            , SQLQueryBuilder deleteById
    ) {
        super(constructor, fieldSetMap);
        this.tableName = tableName;
        this.fieldColumns = fieldColumns;
        this.autoKey = autoKey;
        this.keyFields = keyFields;
        this.count = count;
        this.countById = countById;
        this.select = select;
        this.selectById = selectById;
        this.deleteAll = deleteAll;
        this.deleteById = deleteById;
    }

    public String getTableName() {
        return tableName;
    }

    public FieldColumn[] getFieldColumns() {
        return fieldColumns;
    }

    public Field getAutoKey() {
        return autoKey;
    }

    public Field[] getKeyFields() {
        return keyFields;
    }

    public String getCount() {
        return count;
    }

    public SQLQueryBuilder getCountById() {
        return countById;
    }

    public String getSelect() {
        return select;
    }

    public SQLQueryBuilder getSelectById() {
        return selectById;
    }

    public String getDeleteAll() {
        return deleteAll;
    }

    public SQLQueryBuilder getDeleteById() {
        return deleteById;
    }
}

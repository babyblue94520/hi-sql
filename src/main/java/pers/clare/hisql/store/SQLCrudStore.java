package pers.clare.hisql.store;


import lombok.Getter;
import pers.clare.hisql.function.FieldSetter;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.util.SQLStoreUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

public class SQLCrudStore<T> extends SQLStore<T> {
    @Getter
    private final String tableName;

    @Getter
    private final FieldColumn[] fieldColumns;
    @Getter
    private final Field autoKey;
    @Getter
    private final Field[] keyFields;
    // Force the use of PreparedStatement
    @Getter
    private final boolean ps;

    private String count;
    private SQLQueryBuilder countById;
    private String select;
    private SQLQueryBuilder selectById;
    private String delete;
    private SQLQueryBuilder deleteById;

    public SQLCrudStore(
            Constructor<T> constructor
            , Map<String, FieldSetter> fieldSetMap
            , String tableName
            , FieldColumn[] fieldColumns
            , Field autoKey
            , Field[] keyFields
            , boolean ps
    ) {
        super(constructor, fieldSetMap);
        this.tableName = tableName;
        this.fieldColumns = fieldColumns;
        this.autoKey = autoKey;
        this.keyFields = keyFields;
        this.ps = ps;
    }

    public String getCount() {
        if (count == null) {
            count = "select count(*) from " + tableName;
        }
        return count;
    }

    public SQLQueryBuilder getCountById() {
        if (countById == null) {
            countById = SQLStoreUtil.buildCountById(fieldColumns, tableName);
        }
        return countById;
    }

    public String getSelect() {
        if (select == null) {
            select = SQLStoreUtil.buildSelect(fieldColumns, tableName);
        }
        return select;
    }

    public SQLQueryBuilder getSelectById() {
        if (selectById == null) {
            selectById = SQLStoreUtil.getSelectById(fieldColumns, tableName);
        }
        return selectById;
    }

    public String getDelete() {
        if (delete == null) {
            delete = "delete from " + tableName;
        }
        return delete;
    }

    public SQLQueryBuilder getDeleteById() {
        if (deleteById == null) {
            deleteById = SQLStoreUtil.buildDeleteById(fieldColumns, tableName);
        }
        return deleteById;
    }

}

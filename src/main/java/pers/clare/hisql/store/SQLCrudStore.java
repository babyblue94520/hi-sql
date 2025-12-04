package pers.clare.hisql.store;


import lombok.Getter;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.util.SQLStoreSqlUtil;

import java.lang.reflect.Constructor;

public class SQLCrudStore<T> extends SQLStore<T> {
    @Getter
    private final String tableName;
    @Getter
    private final SQLStoreColumn autoKey;
    @Getter
    private final SQLStoreColumn[] keyColumns;
    // Force the use of PreparedStatement
    @Getter
    private final boolean ps;

    private String count;
    private SQLQueryBuilder countById;
    private String select;
    private SQLQueryBuilder selectById;
    private SQLQueryBuilder selectByIds;
    private String delete;
    private SQLQueryBuilder deleteById;
    private SQLQueryBuilder deleteByIds;

    public SQLCrudStore(
            Constructor<T> constructor
            , String tableName
            , SQLStoreColumn[] columns
            , SQLStoreColumn autoKey
            , SQLStoreColumn[] keyColumns
            , boolean ps
    ) {
        super(constructor, columns);
        this.tableName = tableName;
        this.autoKey = autoKey;
        this.keyColumns = keyColumns;
        this.ps = ps;
    }

    public String getCount() {
        if (count == null) {
            count = SQLStoreSqlUtil.buildCount(tableName);
        }
        return count;
    }

    public SQLQueryBuilder getCountById() {
        if (countById == null) {
            countById = SQLStoreSqlUtil.buildCountById(columns, tableName);
        }
        return countById;
    }

    public String getSelect() {
        if (select == null) {
            select = SQLStoreSqlUtil.buildSelect(columns, tableName);
        }
        return select;
    }

    public SQLQueryBuilder getSelectById() {
        if (selectById == null) {
            selectById = SQLStoreSqlUtil.getSelectById(columns, tableName);
        }
        return selectById;
    }

    public SQLQueryBuilder getSelectByIds() {
        if (selectByIds == null) {
            selectByIds = SQLStoreSqlUtil.getSelectByIds(columns, tableName);
        }
        return selectByIds;
    }

    public String getDelete() {
        if (delete == null) {
            delete = SQLStoreSqlUtil.buildDelete(tableName);
        }
        return delete;
    }

    public SQLQueryBuilder getDeleteById() {
        if (deleteById == null) {
            deleteById = SQLStoreSqlUtil.buildDeleteById(columns, tableName);
        }
        return deleteById;
    }

    public SQLQueryBuilder getDeleteByIds() {
        if (deleteByIds == null) {
            deleteByIds = SQLStoreSqlUtil.buildDeleteByIds(columns, tableName);
        }
        return deleteByIds;
    }
}

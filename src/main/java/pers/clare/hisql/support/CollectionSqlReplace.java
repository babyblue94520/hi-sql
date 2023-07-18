package pers.clare.hisql.support;

import java.util.Collection;

public class CollectionSqlReplace<T> implements SqlReplace<Collection<T>> {
    private final Collection<T> value;
    private final String sql;
    private final String emptySql;

    CollectionSqlReplace(Collection<T> value, String sql, String emptySql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = emptySql;
    }

    CollectionSqlReplace(Collection<T> value, String sql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = null;
    }

    @Override
    public Collection<T> getValue() {
        return value;
    }

    @Override
    public String getSql() {
        return value == null || value.isEmpty() ? emptySql : sql;
    }

}

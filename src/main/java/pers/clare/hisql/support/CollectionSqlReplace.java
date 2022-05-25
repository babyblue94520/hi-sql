package pers.clare.hisql.support;

import java.util.Collection;

public class CollectionSqlReplace implements SqlReplace<Collection<?>> {
    private final Collection<?> value;
    private final String sql;
    private final String emptySql;

    CollectionSqlReplace(Collection<?> value, String sql, String emptySql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = emptySql;
    }

    CollectionSqlReplace(Collection<?> value, String sql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = null;
    }

    @Override
    public Collection<?> getValue() {
        return value;
    }

    @Override
    public String getSql() {
        return value == null || value.isEmpty() ? emptySql : sql;
    }

}

package pers.clare.hisql.support;

public class ObjectSqlReplace implements SqlReplace {
    private final Object value;
    private final String sql;
    private final String emptySql;

    ObjectSqlReplace(Object value, String sql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = null;
    }

    public ObjectSqlReplace(Object value, String sql, String emptySql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = emptySql;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getSql() {
        return value == null ? emptySql : sql;
    }
}

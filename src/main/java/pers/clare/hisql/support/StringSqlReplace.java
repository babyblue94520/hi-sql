package pers.clare.hisql.support;

public class StringSqlReplace implements SqlReplace {
    private final String value;
    private final String sql;
    private final String emptySql;

    StringSqlReplace(String value, String sql, String emptySql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = emptySql;
    }

    StringSqlReplace(String value, String sql) {
        this.value = value;
        this.sql = sql;
        this.emptySql = null;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String getSql() {
        return value == null || value.length() == 0 ? emptySql : sql;
    }

}

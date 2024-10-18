package pers.clare.hisql.store;

public class SQLData {
    private final String sql;
    private final Object[] parameters;

    public SQLData(String sql, Object[] parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getParameters() {
        return parameters;
    }
}

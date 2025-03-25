package pers.clare.hisql.store;

import lombok.Getter;

@Getter
public class SQLData {
    private final String sql;
    private final Object[] parameters;

    public SQLData(String sql, Object[] parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

}

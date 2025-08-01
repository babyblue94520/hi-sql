package pers.clare.hisql.store;

import lombok.Getter;

@Getter
public class SQLRequest {
    private final String sql;
    private final Object[] parameters;

    public SQLRequest(String sql, Object[] parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

}

package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.ResultSet;
import java.sql.Statement;

public class SQLService extends SQLPageService {

    public <T> T insert(
            Class<T> keyType
            , String sql
            , Object... parameters
    ) {
        if (keyType == null) throw new HiSqlException("GeneratedKey type can not null!");
        return this.connection(keyType, sql, parameters, (connection, keyTypeArg, sqlArg, parametersArg) -> {
            Statement statement = ConnectionUtil.insert(connection, sqlArg, parametersArg);
            if (keyTypeArg == void.class) return null;
            if (statement.getUpdateCount() == 0) return null;
            ResultSet rs = statement.getGeneratedKeys();
            return rs.next() ? rs.getObject(1, keyTypeArg) : null;
        });
    }

    public int update(
            String sql
            , Object... parameters
    ) {
        return this.connection(sql, parameters, (connection, sqlArg, parametersArg) -> {
            Statement statement = ConnectionUtil.update(connection, sqlArg, parametersArg);
            return statement.getUpdateCount();
        });
    }

    public long updateLarge(
            String sql
            , Object... parameters
    ) {
        return this.connection(sql, parameters, (connection, sqlArg, parametersArg) -> {
            Statement statement = ConnectionUtil.update(connection, sqlArg, parametersArg);
            return statement.getLargeUpdateCount();
        });
    }

}

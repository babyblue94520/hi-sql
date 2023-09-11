package pers.clare.hisql.repository;

import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.service.SQLService;

@SuppressWarnings("unused")
public class SQLRepositoryImpl<S extends SQLService> implements SQLRepository {
    protected final S sqlService;

    public SQLRepositoryImpl(S sqlService) {
        this.sqlService = sqlService;
    }

    @Override
    public <R> R connection(String sql, Object[] parameters, ConnectionCallback<R> callback) {
        return sqlService.connection(sql, parameters, callback);
    }

    @Override
    public <R> R prepared(String sql, PreparedStatementCallback<R> callback) {
        return sqlService.prepared(sql, callback);
    }

    @Override
    public <R> R resultSet(String sql, Object[] parameters, ResultSetCallback<R> resultSetCallback) {
        return sqlService.query(sql, parameters, resultSetCallback);
    }

    @Override
    public <T> T executeInsert(Class<T> keyType, String sql, Object... args) {
        return sqlService.insert(keyType, sql, args);
    }

    @Override
    public int executeUpdate(String sql, Object... args) {
        return sqlService.update(sql, args);
    }
}

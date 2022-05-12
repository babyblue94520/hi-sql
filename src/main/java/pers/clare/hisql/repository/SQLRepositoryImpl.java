package pers.clare.hisql.repository;

import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.service.SQLService;

public class SQLRepositoryImpl<S extends SQLService> implements SQLRepository {
    protected final S sqlService;

    public SQLRepositoryImpl(S sqlService) {
        this.sqlService = sqlService;
    }

    @Override
    public <R> R connection(String sql, Object[] parameters, ConnectionCallback<R> callback) {
        return sqlService.connection(false, sql, parameters, callback);
    }

    @Override
    public <R> R connection(boolean readonly, String sql, Object[] parameters, ConnectionCallback<R> callback) {
        return sqlService.connection(readonly, sql, parameters, callback);
    }

    @Override
    public <R> R prepared(String sql, PreparedStatementCallback<R> callback) {
        return sqlService.prepared(false, sql, callback);
    }

    @Override
    public <R> R prepared(boolean readonly, String sql, PreparedStatementCallback<R> callback) {
        return sqlService.prepared(readonly, sql, callback);
    }

    @Override
    public <R> R query(String sql, Object[] parameters, ResultSetCallback<R> resultSetCallback) {
        return sqlService.query(false, sql, parameters, resultSetCallback);
    }

    @Override
    public <R> R query(
            boolean readonly
            , String sql
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
    ) {
        return sqlService.query(readonly, sql, parameters, resultSetCallback);
    }

    @Override
    public <R> R insert(String sql, Object[] args, ResultSetCallback<R> callback) {
        return sqlService.insert(sql, args, callback);
    }

    @Override
    public <R> R update(String sql, Object[] args, ResultSetCallback<R> callback) {
        return sqlService.update(sql, args, callback);
    }
}

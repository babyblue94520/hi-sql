package pers.clare.hisql.repository;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.util.ConnectionUtil;

import java.sql.Connection;

public class SQLRepositoryImpl implements SQLRepository {
    private final SQLStoreService sqlStoreService;

    public SQLRepositoryImpl( SQLStoreService sqlStoreService) {
        this.sqlStoreService = sqlStoreService;
    }

    @Override
    public <R> R query(String sql, ResultSetCallback<R> resultSetCallback, Object... parameters) {
        return query(false, sql, resultSetCallback, parameters);
    }

    @Override
    public <R> R query(
            boolean readonly
            , String sql
            , ResultSetCallback<R> resultSetCallback
            , Object... parameters
    ) {
        return sqlStoreService.query(readonly, sql, resultSetCallback, parameters);
    }

    @Override
    public <R> R connection(ConnectionCallback<R> callback) {
        return connection(false, callback);
    }

    @Override
    public <R> R connection(boolean readonly, ConnectionCallback<R> callback) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(readonly);
            return callback.apply(connection);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    @Override
    public <R> R preparedStatement(String sql, PreparedStatementCallback<R> callback) {
        return preparedStatement(false, sql, callback);
    }

    @Override
    public <R> R preparedStatement(boolean readonly, String sql, PreparedStatementCallback<R> callback) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(readonly);
            return callback.apply(connection.prepareStatement(sql));
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }
}

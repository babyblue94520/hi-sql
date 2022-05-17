package pers.clare.hisql.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.util.ConnectionUtil;

import javax.sql.DataSource;
import java.sql.Connection;

public abstract class SQLBasicService {
    protected final HiSqlContext context;
    protected final DataSource dataSource;

    protected SQLBasicService(HiSqlContext context, DataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    public HiSqlContext getContext() {
        return context;
    }

    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    public void closeConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }


    public <R> R connection(
            String sql
            , Object[] parameters
            , ConnectionCallback<R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public <R> R prepared(
            String sql
            , PreparedStatementCallback<R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection.prepareStatement(sql));
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public <R> R query(
            String sql
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return resultSetCallback.apply(ConnectionUtil.query(connection, sql, parameters));
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }
}

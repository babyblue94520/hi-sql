package pers.clare.hisql.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.*;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.util.ConnectionUtil;

import javax.sql.DataSource;
import java.sql.Connection;

public abstract class SQLBasicService {

    private DataSource dataSource;

    private String xmlRoot;

    private PaginationMode paginationMode;

    private NamingStrategy naming;

    private ResultSetConverter resultSetConverter;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getXmlRoot() {
        return xmlRoot;
    }

    public void setXmlRoot(String xmlRoot) {
        this.xmlRoot = xmlRoot;
    }

    public PaginationMode getPaginationMode() {
        return paginationMode;
    }

    public void setPaginationMode(PaginationMode paginationMode) {
        this.paginationMode = paginationMode;
    }

    public NamingStrategy getNaming() {
        return naming;
    }

    public void setNaming(NamingStrategy naming) {
        this.naming = naming;
    }

    public ResultSetConverter getResultSetConverter() {
        return resultSetConverter;
    }

    public void setResultSetConverter(ResultSetConverter resultSetConverter) {
        this.resultSetConverter = resultSetConverter;
    }

    public Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    public void closeConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    public <R> R connection(
            ConnectionOnlyCallback<R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
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
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T, R> R connection(
            Class<T> keyType
            , String sql
            , Object[] parameters
            , ConnectionKeyTypeCallback<T, R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection, keyType, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
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
            throw new HiSqlException(sql, e);
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
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }
}

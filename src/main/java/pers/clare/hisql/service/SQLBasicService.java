package pers.clare.hisql.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.*;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.support.CommandTypeParser;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.support.SqlLogContext;
import pers.clare.hisql.util.PreparedStatementUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Objects;


public interface SQLBasicService {

    DataSource getDataSource();

    String getXmlRoot();

    PaginationMode getPaginationMode();

    NamingStrategy getNaming();

    ResultSetConverter getResultSetConverter();

    CommandTypeParser getCommandTypeParser();

    default Connection getConnection() {
        return DataSourceUtils.getConnection(getDataSource());
    }

    default void releaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, getDataSource());
    }

    default void closeAll(String sql, Connection connection, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
                Statement statement = resultSet.getStatement();
                if (statement != null) statement.close();
            }
        } catch (SQLException e) {
            throw convertToHiSqlException(sql, e);
        }
        releaseConnection(connection);
    }

    default HiSqlException convertToHiSqlException(Exception e) {
        return convertToHiSqlException(null, e);
    }

    default HiSqlException convertToHiSqlException(String sql, Exception e) {
        if (e instanceof HiSqlException) {
            return (HiSqlException) e;
        }
        return new HiSqlException(sql, e);
    }

    default String buildSortSQL(Sort sort, String sql) {
        return getPaginationMode().buildSortSQL(sort, sql);
    }

    default String buildPaginationSQL(Pagination pagination, String sql) {
        return getPaginationMode().buildPaginationSQL(pagination, sql);
    }

    default Pagination getPagination(Pagination pagination) {
        return Objects.requireNonNullElse(pagination, Pagination.of(0, 20));
    }

    default Pagination toPagination(Sort sort) {
        Pagination pagination = getPagination(null);
        if (sort != null) {
            pagination.setSorts(sort.getSorts());
        }
        return pagination;
    }

    default <R> R execute(
            String sql
            , PreparedStatementCallback<R> callback
    ) {
        SqlLogContext.debug(sql);
        Connection connection = null;
        try {
            connection = getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                return callback.apply(stmt);
            }
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            releaseConnection(connection);
        }
    }

    default <R> R query(
            String sql
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
    ) {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            resultSet = query(connection, sql, parameters);
            return resultSetCallback.apply(resultSet);
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            closeAll(sql, connection, resultSet);
        }
    }

    default <T, R> R query(
            String sql
            , Object[] parameters
            , Class<T> clazz
            , ResultSetHandler<T, R> resultSetHandler
    ) throws HiSqlException {
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            resultSet = query(connection, sql, parameters);
            return resultSetHandler.apply(getResultSetConverter(), resultSet, clazz);
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            closeAll(sql, connection, resultSet);
        }
    }

    default ResultSet query(Connection connection, String sql, Object[] parameters) throws SQLException {
        SqlLogContext.debug(sql);
        if (parameters == null || parameters.length == 0) {
            Statement statement = connection.createStatement();
            return statement.executeQuery(sql);
        } else {
            PreparedStatement ps = connection.prepareStatement(sql);
            PreparedStatementUtil.setValue(ps, parameters);
            return ps.executeQuery();
        }
    }

    default <T> T insert(
            Class<T> keyType
            , String sql
            , Object... parameters
    ) {
        SqlLogContext.debug(sql);
        if (keyType == null) throw new HiSqlException("GeneratedKey type can not null!");
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            Statement statement;
            if (parameters == null || parameters.length == 0) {
                statement = connection.createStatement();
                statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatementUtil.setValue(ps, parameters);
                ps.executeUpdate();
                statement = ps;
            }

            if (keyType == void.class) return null;
            if (statement.getUpdateCount() == 0) return null;
            resultSet = statement.getGeneratedKeys();
            return resultSet.next() ? resultSet.getObject(1, keyType) : null;
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            closeAll(sql, connection, resultSet);
        }
    }

    default int update(
            String sql
            , Object... parameters
    ) {
        long longValue = updateLarge(sql, parameters);
        return longValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE > longValue ? Integer.MIN_VALUE : (int) longValue;
    }

    default long updateLarge(
            String sql
            , Object... parameters
    ) {
        SqlLogContext.debug(sql);
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            Statement statement;
            if (parameters == null || parameters.length == 0) {
                statement = connection.createStatement();
                statement.executeUpdate(sql);
            } else {
                PreparedStatement ps = connection.prepareStatement(sql);
                statement = ps;
                PreparedStatementUtil.setValue(ps, parameters);
                ps.executeUpdate();
            }
            return statement.getLargeUpdateCount();
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            closeAll(sql, connection, resultSet);
        }
    }

    default <R> R connection(
            ConnectionOnlyCallback<R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection);
        } catch (Exception e) {
            throw convertToHiSqlException(e);
        } finally {
            releaseConnection(connection);
        }
    }

    default <R> R connection(
            String sql
            , Object[] parameters
            , ConnectionCallback<R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection, sql, parameters);
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            releaseConnection(connection);
        }
    }

    default <T, R> R connection(
            Class<T> keyType
            , String sql
            , Object[] parameters
            , ConnectionKeyTypeCallback<T, R> callback
    ) {
        Connection connection = null;
        try {
            connection = getConnection();
            return callback.apply(connection, keyType, sql, parameters);
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            releaseConnection(connection);
        }
    }

    default <R> R prepared(
            String sql
            , PreparedStatementCallback<R> callback
    ) {
        return execute(sql, callback);
    }

    <T> Page<T> toPage(
            Pagination pagination
            , List<T> list
            , String sql
            , Object[] parameters
    );

}

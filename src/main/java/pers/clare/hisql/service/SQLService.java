package pers.clare.hisql.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.page.*;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLService {

    protected HiSqlContext context;
    protected DataSource write;
    protected DataSource read;

    protected boolean hasRead;

    public SQLService(HiSqlContext context, DataSource write) {
        this(context, write, write);
    }

    public SQLService(
            HiSqlContext context
            , DataSource write
            , DataSource read
    ) {
        this.context = context;
        this.write = write;
        this.read = read;
        hasRead = read != null && write != read;
    }

    public DataSource getDataSource(boolean readonly) {
        return readonly ? read : write;
    }

    public Connection getConnection(DataSource dataSource) throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    protected <T> boolean retry(T result, boolean readonly) {
        return result == null && readonly && hasRead;
    }

    protected <T, R> R queryHandler(
            Boolean readonly
            , String sql
            , Class<T> valueType
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            return doQueryHandler(connection, readonly, sql, valueType, parameters, resultSetHandler);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <R> R query(
            boolean readonly
            , String sql
            , ResultSetCallback<R> resultSetCallback
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            return resultSetCallback.apply(ConnectionUtil.query(connection, sql, parameters));
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    protected <T> Page<T> toPage(
            Pagination pagination
            , List<T> list
            , Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        long total = list.size();
        int size = pagination.getSize();
        int page = pagination.getPage();
        if (total > 0 && total < size) {
            total += (long) size * page;
        } else {
            ResultSet rs = ConnectionUtil.query(connection, context.getPaginationMode().buildTotalSQL(sql), parameters);
            if (rs.next()) {
                total = rs.getLong(1);
            } else {
                throw new HiSqlException("query total error");
            }
        }
        return Page.of(page, size, list, total);
    }

    private <T, R> R doQueryHandler(
            Connection connection
            , Boolean readonly
            , String sql
            , Class<T> valueType
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) throws Exception {
        R result = resultSetHandler.apply(ConnectionUtil.query(connection, sql, parameters), valueType);
        if (retry(result, readonly)) {
            return doQueryHandler(connection, false, sql, valueType, parameters, resultSetHandler);
        } else {
            return result;
        }
    }

    public <T> Map<String, T> find(
            boolean readonly
            , String sql
            , Class<T> valueType
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, Map<String, T>>) ResultSetUtil.toMap);
    }

    public <T> Set<T> findSet(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, Set<T>>) ResultSetUtil.toSet);
    }

    public <T> T findFirst(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, T>) ResultSetUtil.to);
    }

    public <T> Set<Map<String, T>> findAllMapSet(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, Set<Map<String, T>>>) ResultSetUtil.toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, List<Map<String, T>>>) ResultSetUtil.toMapList);
    }

    public <T> List<T> findAll(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, (ResultSetHandler<T, List<T>>) ResultSetUtil.toList);
    }

    public <T> Next<T> basicNext(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            List<T> list = ResultSetUtil.toList(ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters), clazz);
            return Next.of(pagination.getPage(), pagination.getSize(), list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <T> Next<Map<String, T>> next(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            List<Map<String, T>> list = ResultSetUtil.toMapList(ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters), clazz);
            return Next.of(pagination.getPage(), pagination.getSize(), list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <T> Page<T> basicPage(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            List<T> list = ResultSetUtil.toList(ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters), clazz);
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <T> Page<Map<String, T>> page(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return page(false, clazz, sql, pagination, parameters);
    }

    public <T> Page<Map<String, T>> page(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            List<Map<String, T>> list = ResultSetUtil.toMapList(ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters), clazz);
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <T> T insert(
            String sql
            , Class<T> keyType
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(false);
        try {
            connection = getConnection(dataSource);
            Statement statement = ConnectionUtil.insert(connection, sql, parameters);
            if (statement.getUpdateCount() == 0) return null;
            ResultSet rs = statement.getGeneratedKeys();
            return rs.next() ? rs.getObject(1, keyType) : null;
        } catch (SQLException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public int update(
            String sql
            , Object... parameters
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(false);
        try {
            connection = getConnection(dataSource);
            return ConnectionUtil.update(connection, sql, parameters);
        } catch (SQLException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }
}

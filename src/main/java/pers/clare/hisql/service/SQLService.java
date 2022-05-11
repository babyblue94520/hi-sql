package pers.clare.hisql.service;

import org.springframework.jdbc.datasource.DataSourceUtils;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLService {

    protected final HiSqlContext context;
    protected final DataSource write;
    protected final DataSource read;

    protected final boolean hasRead;

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

    public HiSqlContext getContext() {
        return context;
    }

    public DataSource getDataSource(boolean readonly) {
        return readonly ? read : write;
    }

    public Connection getConnection(DataSource dataSource) {
        return DataSourceUtils.getConnection(dataSource);
    }

    protected <T> boolean retry(T result, boolean readonly) {
        return result == null && readonly && hasRead;
    }

    public <R> R connection(
            boolean readonly
            , String sql
            , Object[] parameters
            , ConnectionCallback<R> callback
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            return callback.apply(connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public <R> R prepared(
            boolean readonly
            , String sql
            , Object[] parameters
            , PreparedStatementCallback<R> callback
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            return callback.apply(connection.prepareStatement(sql), parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    protected <T, R> R queryHandler(
            Boolean readonly
            , String sql
            , Class<T> returnType
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(readonly);
        try {
            connection = getConnection(dataSource);
            return doQueryHandler(connection, readonly, sql, returnType, parameters, resultSetHandler);
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
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
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
            , Class<T> returnType
            , Object[] parameters
            , ResultSetHandler<T, R> resultSetHandler
    ) throws Exception {
        R result = resultSetHandler.apply(ConnectionUtil.query(connection, sql, parameters), returnType);
        if (retry(result, readonly)) {
            return doQueryHandler(connection, false, sql, returnType, parameters, resultSetHandler);
        } else {
            return result;
        }
    }

    public <T> Map<String, T> find(
            boolean readonly
            , String sql
            , Class<T> returnType
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, Map<String, T>>) ResultSetUtil.toMap);
    }

    public <T> Set<T> findSet(
            boolean readonly
            , Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, Set<T>>) ResultSetUtil.toSet);
    }

    public <T> T findFirst(
            boolean readonly
            , Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, T>) ResultSetUtil.to);
    }

    public <T> Set<Map<String, T>> findAllMapSet(
            boolean readonly
            , Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, Set<Map<String, T>>>) ResultSetUtil.toMapSet);
    }

    public <T> List<Map<String, T>> findAllMap(
            boolean readonly
            , Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, List<Map<String, T>>>) ResultSetUtil.toMapList);
    }

    public <T> List<T> findAll(
            boolean readonly
            , Class<T> returnType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, returnType, parameters, (ResultSetHandler<T, List<T>>) ResultSetUtil.toList);
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

    public <R> R insert(
            String sql
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(false);
        try {
            connection = getConnection(dataSource);
            Statement statement = ConnectionUtil.insert(connection, sql, parameters);
            return resultSetCallback.apply(statement.getGeneratedKeys());
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

    public <R> R update(
            String sql
            , Object[] parameters
            , ResultSetCallback<R> resultSetCallback
    ) {
        Connection connection = null;
        DataSource dataSource = getDataSource(false);
        try {
            connection = getConnection(dataSource);
            Statement statement = ConnectionUtil.update(connection, sql, parameters);
            return resultSetCallback.apply(statement.getResultSet());
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
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
            Statement statement = ConnectionUtil.update(connection, sql, parameters);
            return statement.getUpdateCount();
        } catch (SQLException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }
}

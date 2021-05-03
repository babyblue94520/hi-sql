package pers.clare.hisql.service;

import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.page.*;
import pers.clare.hisql.support.ConnectionReuse;
import pers.clare.hisql.support.ConnectionReuseHolder;
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

    public Connection getConnection(boolean readonly) throws SQLException {
        ConnectionReuse connectionReuse = ConnectionReuseHolder.get();
        if (connectionReuse == null) {
            return getDataSource(readonly).getConnection();
        } else {
            return connectionReuse.getConnection(getDataSource(readonly && connectionReuse.isReadonly()));
        }
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
        try {
            connection = getConnection(readonly);
            return doQueryHandler(connection, readonly, sql, valueType, parameters, resultSetHandler);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
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

    private <T> T findHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.to(valueType, rs);
    }

    private <T> Map<String, T> findMapHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.toMap(valueType, rs);
    }

    private <T> Set<T> findSetHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.toSet(valueType, rs);
    }

    private <T> Set<Map<String, T>> findAllMapSetHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.toMapSet(valueType, rs);
    }

    private <T> List<Map<String, T>> findAllMapHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.toMapList(valueType, rs);
    }

    private <T> List<T> findAllHandler(ResultSet rs, Class<T> valueType) throws Exception {
        return ResultSetUtil.toList(valueType, rs);
    }

    public <T> Map<String, T> find(
            String sql
            , Class<T> valueType
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findMapHandler);
    }

    public <T> Map<String, T> find(
            boolean readonly
            , String sql
            , Class<T> valueType
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findMapHandler);
    }

    public <T> Set<T> findSet(
            String sql
            , Class<T> valueType
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findSetHandler);
    }

    public <T> Set<T> findSet(
            boolean readonly
            , String sql
            , Class<T> valueType
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findSetHandler);
    }

    public <T> T findFirst(
            Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findHandler);
    }

    public <T> T findFirst(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findHandler);
    }

    public <T> Set<Map<String, T>> findAllMapSet(
            Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findAllMapSetHandler);
    }

    public <T> Set<Map<String, T>> findAllMapSet(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findAllMapSetHandler);
    }

    public <T> List<Map<String, T>> findAllMap(
            Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findAllMapHandler);
    }

    public <T> List<Map<String, T>> findAllMap(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findAllMapHandler);
    }

    public <T> List<T> findAll(
            Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sql, valueType, parameters, this::findAllHandler);
    }

    public <T> List<T> findAll(
            boolean readonly
            , Class<T> valueType
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sql, valueType, parameters, this::findAllHandler);
    }

    public <T> Next<T> basicNext(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return basicNext(false, clazz, sql, pagination, parameters);
    }

    public <T> Next<T> basicNext(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            List<T> list = ResultSetUtil.toList(clazz, ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters));
            return Next.of(pagination.getPage(), pagination.getSize(), list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public <T> Next<Map<String, T>> next(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return next(false, clazz, sql, pagination, parameters);
    }

    public <T> Next<Map<String, T>> next(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            List<Map<String, T>> list = ResultSetUtil.toMapList(clazz, ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters));
            return Next.of(pagination.getPage(), pagination.getSize(), list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public <T> Page<T> basicPage(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return basicPage(false, clazz, sql, pagination, parameters);
    }

    public <T> Page<T> basicPage(
            boolean readonly
            , Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            List<T> list = ResultSetUtil.toList(clazz, ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters));
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
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
        try {
            connection = getConnection(readonly);
            List<Map<String, T>> list = ResultSetUtil.toMapList(clazz, ConnectionUtil.query(connection, context.getPaginationMode().buildPaginationSQL(pagination, sql), parameters));
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public <T> T insert(
            String sql
            , Class<T> keyType
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(false);
            Statement statement = ConnectionUtil.insert(connection, sql, parameters);
            if (statement.getUpdateCount() == 0) return null;
            ResultSet rs = statement.getGeneratedKeys();
            return rs.next() ? rs.getObject(1, keyType) : null;
        } catch (SQLException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public int update(
            String sql
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(false);
            return ConnectionUtil.update(connection, sql, parameters);
        } catch (SQLException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }
}

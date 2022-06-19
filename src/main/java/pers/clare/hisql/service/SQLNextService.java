package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public abstract class SQLNextService extends SQLQueryService {
    public static final Pagination DefaultPagination = Pagination.of(0, 20);

    public SQLNextService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    protected Pagination toPagination(Sort sort) {
        if (sort == null) {
            return DefaultPagination;
        } else {
            return Pagination.of(DefaultPagination.getPage(), DefaultPagination.getSize(), sort.getSorts());
        }
    }

    protected String buildPaginationSQL(Pagination pagination, String sql) {
        if (pagination == null) pagination = DefaultPagination;
        return context.getPaginationMode().buildPaginationSQL(pagination, sql);
    }

    protected <T> Next<T> toNext(Pagination pagination, List<T> list) {
        if (pagination == null) pagination = DefaultPagination;
        return Next.of(pagination.getPage(), pagination.getSize(), list);
    }

    public <T> Next<T> next(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doNext(clazz, sql, null, parameters);
    }

    public <T> Next<T> next(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doNext(clazz, sql, toPagination(sort), parameters);
    }

    public <T> Next<T> next(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doNext(clazz, sql, pagination, parameters);
    }

    private <T> Next<T> doNext(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        String executeSql = buildPaginationSQL(pagination, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            List<T> list = ResultSetUtil.toList(ConnectionUtil.query(connection, executeSql, parameters), clazz);
            return toNext(pagination, list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(executeSql, e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> Next<Map<String, T>> nextMap(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doNextMap(clazz, sql, null, parameters);
    }

    public <T> Next<Map<String, T>> nextMap(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        sql = buildSortSQL(sort, sql);
        return doNextMap(clazz, sql, null, parameters);
    }

    public <T> Next<Map<String, T>> nextMap(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doNextMap(clazz, sql, pagination, parameters);
    }

    private <T> Next<Map<String, T>> doNextMap(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        String executeSql = buildPaginationSQL(pagination, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            List<Map<String, T>> list = ResultSetUtil.toMapList(ConnectionUtil.query(connection, executeSql, parameters), clazz);
            return toNext(pagination, list);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(executeSql, e);
        } finally {
            closeConnection(connection);
        }
    }
}

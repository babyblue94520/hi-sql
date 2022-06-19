package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class SQLPageService extends SQLNextService {
    public SQLPageService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    protected String buildTotalSQL(String sql) {
        return context.getPaginationMode().buildTotalSQL(sql);
    }

    public <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doPage(clazz, sql, null, parameters);
    }

    public <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPage(clazz, sql, toPagination(sort), parameters);
    }

    public <T> Page<T> page(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(clazz, sql, pagination, parameters);
    }

    private <T> Page<T> doPage(
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
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(executeSql, e);
        } finally {
            closeConnection(connection);
        }
    }


    public <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, null, parameters);
    }

    public <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, toPagination(sort), parameters);
    }

    public <T> Page<Map<String, T>> pageMap(
            Class<T> clazz
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPageMap(clazz, sql, pagination, parameters);
    }

    private <T> Page<Map<String, T>> doPageMap(
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
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(executeSql, e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> Page<T> toPage(
            Pagination pagination
            , List<T> list
            , Connection connection
            , String sql
            , Object[] parameters
    ) throws SQLException {
        if (pagination == null) pagination = DefaultPagination;
        long total = list.size();
        int size = pagination.getSize();
        int page = pagination.getPage();
        if (total > 0 && total < size) {
            total += (long) size * page;
        } else {
            String totalSql = buildTotalSQL(sql);
            ResultSet rs = ConnectionUtil.query(connection, totalSql, parameters);
            if (rs.next()) {
                total = rs.getLong(1);
            } else {
                throw new HiSqlException(String.format("query total error.(%s)", totalSql));
            }
        }
        return Page.of(page, size, list, total);
    }
}

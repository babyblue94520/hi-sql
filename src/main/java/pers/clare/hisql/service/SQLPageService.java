package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class SQLPageService extends SQLNextService {

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
        pagination = getPagination(pagination);
        if (pagination.getSize() == 0) return Page.empty(pagination);
        String executeSql = buildPaginationSQL(pagination, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            List<T> list = ResultSetUtil.toList(getResultSetConverter(), ConnectionUtil.query(connection, executeSql, parameters), clazz);
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
        pagination = getPagination(pagination);
        if (pagination.getSize() == 0) return Page.empty(pagination);
        String executeSql = buildPaginationSQL(pagination, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            List<Map<String, T>> list = ResultSetUtil.toMapList(getResultSetConverter(), ConnectionUtil.query(connection, executeSql, parameters), clazz);
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
        int size = pagination.getSize();
        int page = pagination.getPage();
        int listSize = list.size();
        long total = (long) size * page + listSize;

        if (listSize == 0) {
            if (!pagination.isVirtualTotal()) {
                total = getPaginationMode().getTotal(pagination, connection, sql, parameters);
            }
        } else if (listSize >= size) {
            long prevTotal = pagination.getTotal();
            if (total < prevTotal) {
                // if total > 0, then skip count(*).
                total = prevTotal;
            } else {
                if (pagination.isVirtualTotal()) {
                    long virtualTotal = getPaginationMode().getVirtualTotal(pagination, connection, sql, parameters);
                    if (total < virtualTotal) {
                        total = virtualTotal;
                    } else {
                        total += size;
                    }
                } else {
                    total = getPaginationMode().getTotal(pagination, connection, sql, parameters);
                }
            }
        }

        return Page.of(page, size, list, total);
    }

}

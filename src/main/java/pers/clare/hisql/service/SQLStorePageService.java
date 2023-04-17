package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;


public abstract class SQLStorePageService extends SQLStoreNextService {

    public <T> Page<T> page(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(sqlStore, sql, pagination, parameters);
    }


    public <T> Page<T> page(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPage(sqlStore, sql, toPagination(sort), parameters);
    }

    public <T> Page<T> page(
            SQLCrudStore<T> sqlStore
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(sqlStore, sqlStore.getSelect(), pagination, parameters);
    }

    public <T> Page<T> doPage(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        String executeSql = buildPaginationSQL(pagination, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            List<T> list = ResultSetUtil.toInstances(ConnectionUtil.query(connection, executeSql, parameters), sqlStore);
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

}

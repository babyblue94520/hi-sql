package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
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


@SuppressWarnings("unused")
public abstract class SQLStoreNextService extends SQLStoreQueryService {

    public SQLStoreNextService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    public <T> Next<T> next(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return doNext(sqlStore, sql, null, parameters);
    }

    public <T> Next<T> next(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doNext(sqlStore, sql, toPagination(sort), parameters);
    }

    public <T> Next<T> next(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doNext(sqlStore, sql, pagination, parameters);
    }

    public <T> Next<T> next(
            SQLCrudStore<T> sqlStore
            , Pagination pagination
            , Object... parameters
    ) {
        return doNext(sqlStore, sqlStore.getSelect(), pagination, parameters);
    }

    private <T> Next<T> doNext(
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

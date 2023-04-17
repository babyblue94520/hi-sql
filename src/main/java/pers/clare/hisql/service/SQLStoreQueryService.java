package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.StoreResultSetHandler;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.ResultSetUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Set;


public abstract class SQLStoreQueryService extends SQLService {

    private <T, R> R queryHandler(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object[] parameters
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) {
        sql = buildSortSQL(sort, sql);
        Connection connection = null;
        try {
            connection = getConnection();
            return storeResultSetHandler.apply(ConnectionUtil.query(connection, sql, parameters), sqlStore);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(sql, e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> T find(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        String sql = SQLQueryUtil.setValue(sqlStore.getSelectById(), sqlStore.getKeyFields(), entity);
        return queryHandler(sqlStore, sql, null, null, ResultSetUtil::toInstance);
    }

    public <T> T find(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toInstance);
    }

    public <T> T find(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toInstance);
    }

    public <T> Set<T> findSet(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toSetInstance);
    }

    public <T> Set<T> findSet(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toSetInstance);
    }

    public <T> List<T> findAll(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toInstances);
    }

    public <T> List<T> findAll(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toInstances);
    }
}

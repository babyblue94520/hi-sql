package pers.clare.hisql.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.StoreResultSetHandler;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.util.SQLUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Set;


public class SQLStoreService extends SQLService {
    private static final Logger log = LogManager.getLogger();

    public SQLStoreService(HiSqlContext context, DataSource write) {
        super(context, write);
    }

    public SQLStoreService(HiSqlContext context, DataSource write, DataSource read) {
        super(context, write, read);
    }

    private <T, R> R queryHandler(
            Boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Object[] parameters
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            return doQueryHandler(connection, readonly, sqlStore, sql, parameters, storeResultSetHandler);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            close(connection);
        }
    }

    private <T, R> R doQueryHandler(
            Connection connection
            , Boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Object[] parameters
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) throws Exception {
        R result = storeResultSetHandler.apply(go(connection, sql, parameters), sqlStore);
        if (retry(result, readonly)) {
            return queryHandler(false, sqlStore, sql, parameters, storeResultSetHandler);
        } else {
            return result;
        }
    }

    private <T, R> R queryHandler(
            Boolean readonly
            , SQLCrudStore<T> sqlStore
            , T entity
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            return doQueryHandler(connection, readonly, sqlStore, entity, storeResultSetHandler);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            close(connection);
        }
    }

    private <T, R> R doQueryHandler(
            Connection connection
            , Boolean readonly
            , SQLCrudStore<T> sqlStore
            , T entity
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(SQLUtil.setValue(sqlStore.getSelectById(), sqlStore.getKeyFields(), entity));
        R result = storeResultSetHandler.apply(rs, sqlStore);
        if (retry(result, readonly)) {
            statement.close();
            return doQueryHandler(connection, false, sqlStore, entity, storeResultSetHandler);
        } else {
            return result;
        }
    }

    private <T> T findHandler(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        return SQLUtil.toInstance(sqlStore, rs);
    }

    private <T> Set<T> findSetHandler(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        return SQLUtil.toSetInstance(sqlStore, rs);
    }

    private <T> List<T> findAllHandler(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        return SQLUtil.toInstances(sqlStore, rs);
    }

    public <T> T find(
            SQLCrudStore<T> store
            , T entity
    ) {
        return queryHandler(false, store, entity, this::findHandler);
    }

    public <T> T find(
            boolean readonly
            , SQLCrudStore<T> sqlStore
            , T entity
    ) {
        return queryHandler(readonly, sqlStore, entity, this::findHandler);
    }

    public <T> T find(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sqlStore, sql, parameters, this::findHandler);
    }

    public <T> T find(
            boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sqlStore, sql, parameters, this::findHandler);

    }

    public <T> Set<T> findSet(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sqlStore, sql, parameters, this::findSetHandler);
    }

    public <T> Set<T> findSet(
            boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sqlStore, sql, parameters, this::findSetHandler);
    }

    public <T> List<T> findAll(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(false, sqlStore, sql, parameters, this::findAllHandler);
    }

    public <T> List<T> findAll(
            boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(readonly, sqlStore, sql, parameters, this::findAllHandler);
    }

    public <T> List<T> findAll(
            boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(readonly, sqlStore, context.getPageMode().buildSortSQL(sort, sql), parameters, this::findAllHandler);
    }

    public <T> Page<T> page(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return page(false, sqlStore, sql, pagination, parameters);
    }

    public <T> Page<T> page(
            boolean readonly
            , SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            List<T> list = SQLUtil.toInstances(sqlStore, go(connection, context.getPageMode().buildPaginationSQL(pagination, sql), parameters));
            return toPage(pagination, list, connection, sql, parameters);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            close(connection);
        }
    }


    public <T> Page<T> page(
            boolean readonly
            , SQLCrudStore<T> sqlStore
            , Pagination pagination
            , Object... parameters
    ) {
        Connection connection = null;
        try {
            connection = getConnection(readonly);
            List<T> list = SQLUtil.toInstances(sqlStore, go(connection, context.getPageMode().buildPaginationSQL(pagination, sqlStore.getSelect()), parameters));
            long total = list.size();
            if (total < pagination.getSize()) {
                total += pagination.getPage() * pagination.getSize();
            } else {
                ResultSet rs = go(connection, sqlStore.getCount(), parameters);
                if (rs.next()) {
                    total = rs.getLong(1);
                } else {
                    throw new HiSqlException("query total error");
                }
            }
            return Page.of(pagination.getPage(), pagination.getSize(), list, total);
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            close(connection);
        }
    }

}

package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.StoreResultSetHandler;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLRequest;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.util.ResultSetUtil;
import pers.clare.hisql.util.SQLQueryUtil;
import pers.clare.hisql.util.SQLStoreFactory;
import pers.clare.hisql.util.SQLStoreSqlUtil;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SQLStoreService extends SQLBasicService {

    default <T> SQLCrudStore<T> toStore(T entity) {
        return SQLStoreFactory.buildCrud(this, (Class<T>) entity.getClass());
    }

    default <T> T insert(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return null;
        }
        try {
            SQLRequest data = SQLStoreSqlUtil.toInsertRequest(store, entity);
            Field autoKey = store.getAutoKey();
            if (autoKey == null) {
                update(data.getSql(), data.getParameters());
            } else {
                autoKey.set(entity, insert(autoKey.getType(), data.getSql(), data.getParameters()));
            }
            return entity;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    default <T> T[] insertAll(
            SQLCrudStore<T> store
            , T[] entities
    ) {
        insertAll(store, Arrays.asList(entities));
        return entities;
    }

    default <T> Collection<T> insertAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.isEmpty()) return entities;
        for (T entity : entities) {
            insert(store, entity);
        }
        return entities;
    }

    default <T> int update(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return 0;
        }
        try {
            SQLRequest data = SQLStoreSqlUtil.toUpdateRequest(store, entity);
            return update(data.getSql(), data.getParameters());
        } catch (Exception e) {
            throw convertToHiSqlException(e);
        }
    }

    default <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        return updateAll(sqlStore, Arrays.asList(entities));
    }

    default <T> int[] updateAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.isEmpty()) return new int[0];
        int[] counts = new int[entities.size()];
        int i = 0;
        for (T entity : entities) {
            counts[i++] = update(store, entity);
        }
        return counts;
    }

    default <T> int delete(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return update(SQLQueryUtil.setValue(store.getDeleteById(), store.getKeyFields(), entity));
    }

    default <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        return deleteAll(sqlStore, Arrays.asList(entities));
    }

    default <T> int[] deleteAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.isEmpty()) return new int[0];
        int[] counts = new int[entities.size()];
        int i = 0;
        for (T entity : entities) {
            counts[i++] = delete(store, entity);
        }
        return counts;
    }

    default <T> T findByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return find(toStore(entity), entity);
    }

    default <T> T insertByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return insert(toStore(entity), entity);
    }

    default <T> int updateByObject(
            T entity
    ) {
        if (entity == null) {
            return 0;
        }
        if (entity instanceof String) {
            return update((String) entity);
        } else {
            return update(toStore(entity), entity);
        }
    }

    default <T> int deleteByObject(
            T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return delete(toStore(entity), entity);
    }


    private <T, R> R queryHandler(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object[] parameters
            , StoreResultSetHandler<T, R> storeResultSetHandler
    ) {
        return query(buildSortSQL(sort, sql), parameters, sqlStore, storeResultSetHandler);
    }

    default <T, R> R query(
            String sql
            , Object[] parameters
            , SQLStore<T> sqlStore
            , StoreResultSetHandler<T, R> function
    ) throws HiSqlException {
        logSql(sql);
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            resultSet = query(connection, sql, parameters);
            return function.apply(sqlStore, resultSet);
        } catch (Exception e) {
            throw convertToHiSqlException(sql, e);
        } finally {
            closeAll(sql, connection, resultSet);
        }
    }

    default <T> T find(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        String sql = SQLQueryUtil.setValue(sqlStore.getSelectById(), sqlStore.getKeyFields(), entity);
        return queryHandler(sqlStore, sql, null, null, ResultSetUtil::toInstance);
    }

    default <T> T find(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toInstance);
    }

    default <T> T find(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toInstance);
    }

    default <T> Set<T> findSet(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toSetInstance);
    }

    default <T> Set<T> findSet(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toSetInstance);
    }

    default <T> List<T> findAll(
            SQLStore<T> sqlStore
            , String sql
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, null, parameters, ResultSetUtil::toInstances);
    }

    default <T> List<T> findAll(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return queryHandler(sqlStore, sql, sort, parameters, ResultSetUtil::toInstances);
    }


    default <T> Page<T> page(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(sqlStore, sql, pagination, parameters);
    }

    default <T> Page<T> page(
            SQLStore<T> sqlStore
            , String sql
            , Sort sort
            , Object... parameters
    ) {
        return doPage(sqlStore, sql, toPagination(sort), parameters);
    }

    default <T> Page<T> page(
            SQLCrudStore<T> sqlStore
            , Pagination pagination
            , Object... parameters
    ) {
        return doPage(sqlStore, sqlStore.getSelect(), pagination, parameters);
    }

    default <T> Page<T> doPage(
            SQLStore<T> sqlStore
            , String sql
            , Pagination pagination
            , Object... parameters
    ) {
        pagination = getPagination(pagination);
        if (pagination.getSize() == 0) return Page.empty(pagination);
        String executeSql = buildPaginationSQL(pagination, sql);
        List<T> list = query(executeSql, parameters, sqlStore, ResultSetUtil::toInstances);
        return toPage(pagination, list, sql, parameters);
    }
}

package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLData;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.SQLQueryUtil;
import pers.clare.hisql.util.SQLStoreUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;


public class SQLStoreService extends SQLStorePageService {

    public <T> T insert(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return null;
        }
        try {
            Field autoKey = store.getAutoKey();
            if (store.isPs()) {
                SQLData data = SQLStoreUtil.toInsertSQLData(store, entity);
                if (autoKey == null) {
                    update(data.getSql(), data.getParameters());
                } else {
                    autoKey.set(entity, insert(autoKey.getType(), data.getSql(), data.getParameters()));
                }
            } else {
                String sql = SQLStoreUtil.buildInsertSQL(store, entity);
                if (autoKey == null) {
                    update(sql);
                } else {
                    autoKey.set(entity, insert(autoKey.getType(), sql));
                }
            }
            return entity;
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public <T> T[] insertAll(
            SQLCrudStore<T> store
            , T[] entities
    ) {
        insertAll(store, Arrays.asList(entities));
        return entities;
    }

    public <T> Collection<T> insertAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return entities;
        for (T entity : entities) {
            insert(store, entity);
        }
        return entities;
    }

    public <T> int update(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return 0;
        }
        if (store.isPs()) {
            try {
                SQLData data = SQLStoreUtil.toUpdateSQLData(store, entity);
                return update(data.getSql(), data.getParameters());
            } catch (IllegalAccessException e) {
                throw new HiSqlException(e);
            }
        } else {
            return update(SQLStoreUtil.buildUpdateSQL(store, entity));
        }
    }

    public final <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        return updateAll(sqlStore, Arrays.asList(entities));
    }

    public <T> int[] updateAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        int[] counts = new int[entities.size()];
        int i = 0;
        for (T entity : entities) {
            counts[i++] = update(store, entity);
        }
        return counts;
    }

    public <T> int delete(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return update(SQLQueryUtil.setValue(store.getDeleteById(), store.getKeyFields(), entity));
    }

    public <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        return deleteAll(sqlStore, Arrays.asList(entities));
    }

    public <T> int[] deleteAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        int[] counts = new int[entities.size()];
        int i = 0;
        for (T entity : entities) {
            counts[i++] = delete(store, entity);
        }
        return counts;
    }

    public <T> T findByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return find(toStore(entity), entity);
    }

    public <T> T insertByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return insert(toStore(entity), entity);
    }

    public <T> int updateByObject(
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

    public <T> int deleteByObject(
            T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return delete(toStore(entity), entity);
    }

    private <T> SQLCrudStore<T> toStore(T entity) {
        return SQLStoreFactory.buildCrud(getNaming(), getResultSetConverter(), (Class<T>) entity.getClass());
    }

}

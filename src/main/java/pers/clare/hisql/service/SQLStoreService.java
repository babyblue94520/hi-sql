package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.SQLQueryUtil;
import pers.clare.hisql.util.SQLStoreUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.Statement;
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
            String sql = SQLStoreUtil.buildInsertSQL(store, entity);
            if (autoKey == null) {
                update(sql);
            } else {
                autoKey.set(entity, insert(autoKey.getType(), sql));
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
        if (entities != null && entities.length > 0) {
            insertAll(store, Arrays.asList(entities));
        }
        return entities;
    }

    public <T> Collection<T> insertAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return entities;
        return this.connection((connection) -> {
            Field autoKey = store.getAutoKey();
            Statement statement = connection.createStatement();
            if (autoKey == null) {
                for (T entity : entities) {
                    ConnectionUtil.update(connection, SQLStoreUtil.buildInsertSQL(store, entity));
                }
            } else {
                for (T entity : entities) {
                    int count = statement.executeUpdate(SQLStoreUtil.buildInsertSQL(store, entity), Statement.RETURN_GENERATED_KEYS);
                    if (count > 0) {
                        ResultSet rs = statement.getGeneratedKeys();
                        autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                    }
                }
            }
            return entities;
        });
    }

    public <T> int update(
            SQLCrudStore<T> store
            , T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return update(SQLStoreUtil.buildUpdateSQL(store, entity));
    }

    public final <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        return updateAll(sqlStore, Arrays.asList(entities));
    }

    public <T> int[] updateAll(
            SQLCrudStore<T> store
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        return this.connection((connection) -> {
            int[] counts = new int[entities.size()];
            int i = 0;
            Statement statement = connection.createStatement();
            for (T entity : entities) {
                counts[i++] = statement.executeUpdate(SQLStoreUtil.buildUpdateSQL(store, entity));
            }
            return counts;
        });
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
        return this.connection((connection) -> {
            int[] counts = new int[entities.size()];
            int i = 0;
            Statement statement = connection.createStatement();
            for (T entity : entities) {
                counts[i++] = statement.executeUpdate(SQLQueryUtil.setValue(store.getDeleteById(), store.getKeyFields(), entity));
            }
            return counts;
        });
    }

    public <T> T findByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return find(
                SQLStoreFactory.buildCrud(getNaming(), getResultSetConverter(), (Class<T>) entity.getClass())
                , entity
        );
    }

    public <T> T insertByObject(
            T entity
    ) {
        if (entity == null) {
            return null;
        }
        return insert(
                SQLStoreFactory.buildCrud(getNaming(), getResultSetConverter(), (Class<T>) entity.getClass())
                , entity
        );
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
            return update(
                    SQLStoreFactory.buildCrud(getNaming(), getResultSetConverter(), (Class<T>) entity.getClass())
                    , entity
            );
        }
    }

    public <T> int deleteByObject(
            T entity
    ) {
        if (entity == null) {
            return 0;
        }
        return delete(
                SQLStoreFactory.buildCrud(getNaming(), getResultSetConverter(), (Class<T>) entity.getClass())
                , entity
        );
    }

}

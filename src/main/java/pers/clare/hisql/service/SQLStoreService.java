package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLData;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.SQLQueryUtil;
import pers.clare.hisql.util.SQLStoreUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;


public class SQLStoreService extends SQLStorePageService {

    public <T> T insert(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        try {
            SQLData sqlData = SQLStoreUtil.toInsertSQLData(sqlStore, entity);
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                update(sqlData.getSql(), sqlData.getParameters());
            } else {
                autoKey.set(entity, insert(autoKey.getType(), sqlData.getSql(), sqlData.getParameters()));
            }
            return entity;
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public <T> Collection<T> insertAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        return this.connection((connection) -> {
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (T entity : entities) {
                    SQLData sqlData = SQLStoreUtil.toInsertSQLData(sqlStore, entity);
                    PreparedStatement statement = connection.prepareStatement(sqlData.getSql());
                    ConnectionUtil.insert(statement, sqlData.getParameters());
                }
            } else {
                ResultSet rs;
                for (T entity : entities) {
                    SQLData sqlData = SQLStoreUtil.toInsertSQLData(sqlStore, entity);
                    PreparedStatement statement = connection.prepareStatement(sqlData.getSql(), Statement.RETURN_GENERATED_KEYS);
                    ConnectionUtil.insert(statement, sqlData.getParameters());
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        });
    }

    public <T> T[] insertAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        return this.connection((connection) -> {
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (T entity : entities) {
                    SQLData sqlData = SQLStoreUtil.toInsertSQLData(sqlStore, entity);
                    PreparedStatement statement = connection.prepareStatement(sqlData.getSql());
                    ConnectionUtil.insert(statement, sqlData.getParameters());
                }
            } else {
                ResultSet rs;
                for (T entity : entities) {
                    SQLData sqlData = SQLStoreUtil.toInsertSQLData(sqlStore, entity);
                    PreparedStatement statement = connection.prepareStatement(sqlData.getSql(), Statement.RETURN_GENERATED_KEYS);
                    ConnectionUtil.insert(statement, sqlData.getParameters());
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        });
    }

    public <T> int update(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        try {
            SQLData sqlData = SQLStoreUtil.toUpdateSQLData(sqlStore, entity);
            return update(sqlData.getSql(), sqlData.getParameters());
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        return this.connection((connection) -> {
            int[] counts = new int[entities.size()];
            int i = 0;
            for (T entity : entities) {
                SQLData sqlData = SQLStoreUtil.toUpdateSQLData(sqlStore, entity);
                PreparedStatement statement = connection.prepareStatement(sqlData.getSql());
                counts[i++] = ConnectionUtil.update(statement, sqlData.getParameters());
            }
            return counts;
        });
    }

    public final <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        return this.connection((connection) -> {
            int[] counts = new int[entities.length];
            int i = 0;
            for (T entity : entities) {
                SQLData sqlData = SQLStoreUtil.toUpdateSQLData(sqlStore, entity);
                PreparedStatement statement = connection.prepareStatement(sqlData.getSql());
                counts[i++] = ConnectionUtil.update(statement, sqlData.getParameters());
            }
            return counts;
        });
    }


    public <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        return this.connection((connection) -> {
            int[] counts = new int[entities.size()];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = update(SQLQueryUtil.setValue(sqlStore.getDeleteById(), sqlStore.getKeyFields(), entity));
            }
            return counts;
        });
    }

    public <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        return this.connection((connection) -> {
            int[] counts = new int[entities.length];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = update(SQLQueryUtil.setValue(sqlStore.getDeleteById(), sqlStore.getKeyFields(), entity));
            }
            return counts;
        });
    }
}

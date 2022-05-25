package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.SQLStoreUtil;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;


public class SQLStoreService extends SQLStorePageService {
    @SuppressWarnings("unused")
    public SQLStoreService(HiSqlContext context, DataSource dataSource) {
        super(context, dataSource);
    }

    public <T> T insert(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        try {
            String sql = SQLStoreUtil.toInsertSQL(sqlStore, entity);
            Field autoKey = sqlStore.getAutoKey();
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

    public <T> Collection<T> insertAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, SQLStoreUtil.toInsertSQL(sqlStore, entity));
                }
            } else {
                ResultSet rs;
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, SQLStoreUtil.toInsertSQL(sqlStore, entity));
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> T[] insertAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, SQLStoreUtil.toInsertSQL(sqlStore, entity));
                }
            } else {
                ResultSet rs;
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, SQLStoreUtil.toInsertSQL(sqlStore, entity));
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> int update(
            SQLCrudStore<T> sqlStore
            , T entity
    ) {
        try {
            return update(SQLStoreUtil.toUpdateSQL(sqlStore, entity));
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.size()];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, SQLStoreUtil.toUpdateSQL(sqlStore, entity));
            }
            return counts;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public final <T> int[] updateAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.length];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, SQLStoreUtil.toUpdateSQL(sqlStore, entity));
            }
            return counts;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }


    public <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , Collection<T> entities
    ) {
        if (entities == null || entities.size() == 0) return new int[0];
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.size()];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, SQLStoreUtil.toDeleteSQL(sqlStore, entity));
            }
            return counts;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }

    public <T> int[] deleteAll(
            SQLCrudStore<T> sqlStore
            , T[] entities
    ) {
        if (entities == null || entities.length == 0) return new int[0];
        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.length];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, SQLStoreUtil.toDeleteSQL(sqlStore, entity));
            }
            return counts;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        } finally {
            closeConnection(connection);
        }
    }
}

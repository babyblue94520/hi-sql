package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.FieldColumn;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.ConnectionUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.Collection;
import java.util.List;


public class SQLCrudRepositoryImpl<T> implements SQLCrudRepository<T> {
    private static final Logger log = LogManager.getLogger();
    private final SQLCrudStore<T> sqlStore;
    private final SQLStoreService sqlStoreService;

    public SQLCrudRepositoryImpl(HiSqlContext context, SQLStoreService sqlStoreService, Class<T> repositoryClass) {
        this.sqlStoreService = sqlStoreService;
        ParameterizedType[] interfaces = (ParameterizedType[]) repositoryClass.getGenericInterfaces();
        if (interfaces.length == 0) {
            throw new IllegalArgumentException("SQLCrudRepository interface must not be null!");
        }
        ParameterizedType parameterizedType = null;
        for (ParameterizedType anInterface : interfaces) {
            if (anInterface.getRawType() == SQLCrudRepository.class) {
                parameterizedType = anInterface;
                break;
            }
        }
        if (parameterizedType == null) {
            throw new IllegalArgumentException("SQLCrudRepository interface not found!");
        }
        Type[] types = parameterizedType.getActualTypeArguments();
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("SQLCrudRepository entity class must not be null!");
        }
        sqlStore = (SQLCrudStore<T>) SQLStoreFactory.build(context, (Class<T>) types[0], true);
    }

    public long count() {
        return count(false);
    }

    public long count(
            Boolean readonly
    ) {
        Long count = sqlStoreService.findFirst(readonly, Long.class, sqlStore.getCount());
        return count == null ? 0 : count;
    }

    public long count(T entity) {
        return count(false, entity);
    }

    public long count(
            Boolean readonly
            , T entity
    ) {
        try {
            Long count = sqlStoreService.findFirst(readonly, Long.class, SQLQueryUtil.setValue(sqlStore.getCountById(), sqlStore.getKeyFields(), entity));
            return count == null ? 0 : count;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public long countById(Object... ids) {
        return countById(false, ids);
    }

    public long countById(
            Boolean readonly
            , Object... ids
    ) {
        try {
            Long count = sqlStoreService.findFirst(readonly, Long.class, SQLQueryUtil.setValue(sqlStore.getCountById(), sqlStore.getKeyFields(), ids));
            return count == null ? 0 : count;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public List<T> findAll(
    ) {
        return findAll(false);
    }

    public List<T> findAll(
            Sort sort
    ) {
        return sqlStoreService.findAll(false, sqlStore, sqlStore.getSelect(), sort);
    }

    @Override
    public Page<T> page(Pagination pagination) {
        return sqlStoreService.page(false, sqlStore, pagination);
    }

    @Override
    public Next<T> next(Pagination pagination) {
        return sqlStoreService.next(false, sqlStore, pagination);
    }

    public List<T> findAll(
            Boolean readonly
    ) {
        return sqlStoreService.findAll(readonly, sqlStore, sqlStore.getSelect());
    }

    public T findById(Object... ids) {
        return findById(false, ids);
    }

    public T findById(
            Boolean readonly
            , Object... ids
    ) {
        return sqlStoreService.find(readonly, sqlStore, SQLQueryUtil.setValue(sqlStore.getSelectById(), sqlStore.getKeyFields(), ids));
    }

    public T find(
            T entity
    ) {
        return find(false, entity);
    }

    public T find(
            Boolean readonly
            , T entity
    ) {
        return sqlStoreService.find(readonly, sqlStore, entity);
    }

    public T insert(
            T entity
    ) {
        try {
            String sql = toInsertSQL(sqlStore, entity);
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                sqlStoreService.update(sql);
            } else {
                autoKey.set(entity, sqlStoreService.insert(sql, autoKey.getType()));
            }
            return entity;
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public int update(
            T entity
    ) {
        try {
            return sqlStoreService.update(toUpdateSQL(sqlStore, entity));
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public int delete(
            T entity
    ) {
        return sqlStoreService.update(SQLQueryUtil.setValue(sqlStore.getDeleteById(), sqlStore.getKeyFields(), entity));
    }

    public int deleteById(
            Object... id
    ) {
        return sqlStoreService.update(SQLQueryUtil.setValue(sqlStore.getDeleteById(), sqlStore.getKeyFields(), id));
    }

    @Override
    public Collection<T> insertAll(Collection<T> entities) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(false);
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (T entity : entities) {
                    sqlStoreService.update(toInsertSQL(sqlStore, entity));
                }
            } else {
                for (T entity : entities) {
                    autoKey.set(entity, sqlStoreService.insert(toInsertSQL(sqlStore, entity), autoKey.getType()));
                }
            }
            return entities;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    @Override
    public T[] insertAll(T[] entities) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(false);
            Field autoKey = sqlStore.getAutoKey();
            Statement statement = connection.createStatement();
            if (autoKey == null) {
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, toInsertSQL(sqlStore, entity));
                }
            } else {
                ResultSet rs;
                for (T entity : entities) {
                    ConnectionUtil.insert(statement, toInsertSQL(sqlStore, entity));
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    @Override
    public int[] updateAll(Collection<T> entities) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(false);
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.size()];
            int i = 0;
            for (T entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, toUpdateSQL(sqlStore, entity));
            }
            return counts;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    @Override
    public int[] updateAll(T[] entities) {
        Connection connection = null;
        try {
            connection = sqlStoreService.getConnection(false);
            Statement statement = connection.createStatement();
            int l = entities.length;
            int[] counts = new int[l];
            for (int i = 0; i < l; i++) {
                counts[i] = ConnectionUtil.update(statement, toUpdateSQL(sqlStore, entities[i]));
            }
            return counts;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    public int deleteAll() {
        return sqlStoreService.update(sqlStore.getDeleteAll());
    }

    private static String toInsertSQL(SQLCrudStore sqlStore, Object entity) throws IllegalAccessException {
        FieldColumn[] fieldColumns = sqlStore.getFieldColumns();
        StringBuilder columns = new StringBuilder("insert into " + sqlStore.getTableName() + "(");
        StringBuilder values = new StringBuilder("values(");
        Object value;
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null || !fieldColumn.isInsertable()) continue;
            value = fieldColumn.getField().get(entity);
            if (value == null) {
                if (fieldColumn.isAuto()) continue;
                if (!fieldColumn.isNullable()) continue;
            }
            columns.append(fieldColumn.getColumnName())
                    .append(',');

            SQLQueryUtil.appendValue(values, value);
            values.append(',');
        }
        values.deleteCharAt(values.length() - 1).append(')');
        columns.deleteCharAt(columns.length() - 1)
                .append(')')
                .append(values);
        return columns.toString();
    }

    private static String toUpdateSQL(SQLCrudStore sqlStore, Object entity) throws IllegalAccessException {
        FieldColumn[] fieldColumns = sqlStore.getFieldColumns();
        StringBuilder values = new StringBuilder("update " + sqlStore.getTableName() + " set ");
        StringBuilder wheres = new StringBuilder(" where ");
        Object value;
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null) continue;
            value = fieldColumn.getField().get(entity);
            if (fieldColumn.isId()) {
                if (value == null) {
                    wheres.append(fieldColumn.getColumnName())
                            .append(" is null");
                } else {
                    wheres.append(fieldColumn.getColumnName())
                            .append('=');
                    SQLQueryUtil.appendValue(wheres, value);
                }
                wheres.append(" and ");
            } else {
                if (!fieldColumn.isUpdatable()) continue;
                if (value == null && !fieldColumn.isNullable()) continue;

                values.append(fieldColumn.getColumnName())
                        .append('=');
                SQLQueryUtil.appendValue(values, value);
                values.append(',');
            }
        }
        wheres.delete(wheres.length() - 5, wheres.length() - 1);
        values.deleteCharAt(values.length() - 1)
                .append(wheres);
        return values.toString();
    }
}

package pers.clare.hisql.repository;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.KeySQLBuilder;
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

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class SQLCrudRepositoryImpl<Entity, Key> extends SQLRepositoryImpl<SQLStoreService> implements SQLCrudRepository<Entity, Key> {
    protected final SQLCrudStore<Entity> sqlStore;
    protected final KeySQLBuilder<Key> keySQLBuilder;

    public SQLCrudRepositoryImpl(SQLStoreService sqlService, Class<Entity> repositoryClass) {
        super(sqlService);
        Type[] types = findTypes(repositoryClass);
        Class<Entity> entityClass = (Class<Entity>) types[0];
        sqlStore = SQLStoreFactory.build(sqlService.getContext(), entityClass, true);

        Class<Key> keyClass = (Class<Key>) types[1];
        keySQLBuilder = SQLStoreFactory.buildKey(keyClass, sqlStore);
    }


    public long count() {
        return count(false);
    }

    public long count(
            Boolean readonly
    ) {
        Long count = sqlService.findFirst(readonly, Long.class, sqlStore.getCount());
        return count == null ? 0 : count;
    }

    public long count(Entity entity) {
        return count(false, entity);
    }

    public long count(Boolean readonly, Entity entity) {
        try {
            Long count = sqlService.findFirst(readonly, Long.class, SQLQueryUtil.setValue(sqlStore.getCountById(), sqlStore.getKeyFields(), entity));
            return count == null ? 0 : count;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public long countById(Key key) {
        return countById(false, key);
    }

    public long countById(
            Boolean readonly
            , Key key
    ) {
        try {
            Long count = sqlService.findFirst(readonly, Long.class, keySQLBuilder.apply(sqlStore.getCountById(), key));
            return count == null ? 0 : count;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public List<Entity> findAll(
    ) {
        return findAll(false);
    }

    public List<Entity> findAll(
            Sort sort
    ) {
        return sqlService.findAll(false, sqlStore, sqlStore.getSelect(), sort);
    }

    @Override
    public Page<Entity> page(Pagination pagination) {
        return sqlService.page(false, sqlStore, pagination);
    }

    @Override
    public Next<Entity> next(Pagination pagination) {
        return sqlService.next(false, sqlStore, pagination);
    }

    public List<Entity> findAll(
            Boolean readonly
    ) {
        return sqlService.findAll(readonly, sqlStore, sqlStore.getSelect());
    }

    public Entity findById(Key key) {
        return findById(false, key);
    }

    public Entity findById(
            Boolean readonly
            , Key key
    ) {
        return sqlService.find(readonly, sqlStore, keySQLBuilder.apply(sqlStore.getSelectById(), key));
    }

    public Entity find(
            Entity entity
    ) {
        return find(false, entity);
    }

    public Entity find(
            Boolean readonly
            , Entity entity
    ) {
        return sqlService.find(readonly, sqlStore, entity);
    }

    public Entity insert(
            Entity entity
    ) {
        try {
            String sql = toInsertSQL(sqlStore, entity);
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                sqlService.update(sql);
            } else {
                autoKey.set(entity, sqlService.insert(sql, autoKey.getType()));
            }
            return entity;
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public int update(
            Entity entity
    ) {
        try {
            return sqlService.update(toUpdateSQL(sqlStore, entity));
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        }
    }

    public int delete(
            Entity entity
    ) {
        return sqlService.update(SQLQueryUtil.setValue(sqlStore.getDeleteById(), sqlStore.getKeyFields(), entity));
    }

    public int deleteById(
            Key key
    ) {
        return sqlService.update(keySQLBuilder.apply(sqlStore.getDeleteById(), key));
    }

    @Override
    public Collection<Entity> insertAll(Collection<Entity> entities) {
        Connection connection = null;
        DataSource dataSource = sqlService.getDataSource(false);
        try {
            connection = sqlService.getConnection(dataSource);
            Field autoKey = sqlStore.getAutoKey();
            if (autoKey == null) {
                for (Entity entity : entities) {
                    sqlService.update(toInsertSQL(sqlStore, entity));
                }
            } else {
                for (Entity entity : entities) {
                    autoKey.set(entity, sqlService.insert(toInsertSQL(sqlStore, entity), autoKey.getType()));
                }
            }
            return entities;
        } catch (IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    @Override
    public Entity[] insertAll(Entity[] entities) {
        Connection connection = null;
        DataSource dataSource = sqlService.getDataSource(false);
        try {
            connection = sqlService.getConnection(dataSource);
            Field autoKey = sqlStore.getAutoKey();
            Statement statement = connection.createStatement();
            if (autoKey == null) {
                for (Entity entity : entities) {
                    ConnectionUtil.insert(statement, toInsertSQL(sqlStore, entity));
                }
            } else {
                ResultSet rs;
                for (Entity entity : entities) {
                    ConnectionUtil.insert(statement, toInsertSQL(sqlStore, entity));
                    rs = statement.getGeneratedKeys();
                    autoKey.set(entity, rs.next() ? rs.getObject(1, autoKey.getType()) : null);
                }
            }
            return entities;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    @Override
    public int[] updateAll(Collection<Entity> entities) {
        Connection connection = null;
        DataSource dataSource = sqlService.getDataSource(false);
        try {
            connection = sqlService.getConnection(dataSource);
            Statement statement = connection.createStatement();
            int[] counts = new int[entities.size()];
            int i = 0;
            for (Entity entity : entities) {
                counts[i++] = ConnectionUtil.update(statement, toUpdateSQL(sqlStore, entity));
            }
            return counts;
        } catch (SQLException | IllegalAccessException e) {
            throw new HiSqlException(e);
        } finally {
            ConnectionUtil.close(connection, dataSource);
        }
    }

    @Override
    public int[] updateAll(Entity[] entities) {
        Connection connection = null;
        DataSource dataSource = sqlService.getDataSource(false);
        try {
            connection = sqlService.getConnection(dataSource);
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
            ConnectionUtil.close(connection, dataSource);
        }
    }

    public int deleteAll() {
        return sqlService.update(sqlStore.getDeleteAll());
    }

    private static Type[] findTypes(Class<?> clazz) {
        Map<Class<?>, Type[]> typesMap = new HashMap<>();
        Type[] types = findTypes(clazz, typesMap);
        if (types == null) {
            throw new IllegalArgumentException(String.format("%s entity class not found!", clazz));
        }
        for (Type type : types) {
            if (!(type instanceof Class)) {
                throw new IllegalArgumentException(String.format("%s %s class not found!", clazz, type));
            }
        }
        return types;
    }

    private static Type[] findTypes(Class<?> clazz, Map<Class<?>, Type[]> typesMap) {
        Type[] types = null;
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                typesMap.put((Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
                if (parameterizedType.getRawType() == SQLCrudRepository.class) {
                    types = parameterizedType.getActualTypeArguments();
                } else {
                    types = findTypes((Class<?>) parameterizedType.getRawType(), typesMap);
                }
                findTypeVariableToClass(types, clazz, typesMap);
            } else if (type instanceof Class) {
                types = findTypes((Class<?>) type, typesMap);
                findTypeVariableToClass(types, clazz, typesMap);
            }
        }
        return types;
    }

    private static void findTypeVariableToClass(Type[] types, Class<?> clazz, Map<Class<?>, Type[]> typesMap) {
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            if (type instanceof TypeVariable) {
                for (int j = 0; j < clazz.getTypeParameters().length; j++) {
                    if (type.getTypeName().equals(clazz.getTypeParameters()[j].getTypeName())) {
                        types[i] = typesMap.get(clazz)[j];
                    }
                }
            }
        }
    }

    private static String toInsertSQL(SQLCrudStore<?> sqlStore, Object entity) throws IllegalAccessException {
        FieldColumn[] fieldColumns = sqlStore.getFieldColumns();
        StringBuilder columns = new StringBuilder("insert into " + sqlStore.getTableName() + "(");
        StringBuilder values = new StringBuilder("values(");
        Object value;
        for (FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn == null || !fieldColumn.isInsertable()) continue;
            value = fieldColumn.getField().get(entity);
            if (value == null) {
                if (fieldColumn.isAuto()) continue;
                if (fieldColumn.isNotNullable()) continue;
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

    private static String toUpdateSQL(SQLCrudStore<?> sqlStore, Object entity) throws IllegalAccessException {
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
                if (value == null && fieldColumn.isNotNullable()) continue;

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

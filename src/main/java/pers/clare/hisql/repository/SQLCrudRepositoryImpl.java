package pers.clare.hisql.repository;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.KeySQLBuilder;
import pers.clare.hisql.function.KeysSQLBuilder;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreColumn;
import pers.clare.hisql.util.SQLQueryUtil;
import pers.clare.hisql.util.SQLStoreColumnUtil;
import pers.clare.hisql.util.SQLStoreFactory;
import pers.clare.hisql.util.TypeUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class SQLCrudRepositoryImpl<E, K> extends SQLRepositoryImpl<SQLService> implements SQLCrudRepository<E, K> {

    protected final SQLCrudStore<E> sqlStore;
    protected final KeySQLBuilder<K> keySQLBuilder;
    protected final KeysSQLBuilder<K> keysSQLBuilder;
    protected final Class<K> keyClass;
    protected final SQLStoreColumn[] keyColumns;

    @SuppressWarnings("unchecked")
    public SQLCrudRepositoryImpl(SQLService sqlService, Class<E> repositoryClass) {
        super(sqlService);
        Type[] types = TypeUtil.findTypes(repositoryClass);
        Class<E> entityClass = (Class<E>) types[0];
        sqlStore = SQLStoreFactory.buildCrud(sqlService, entityClass);
        keyClass = (Class<K>) types[1];

        if (TypeUtil.isBasicType(keyClass)
            || TypeUtil.isBasicTypeArray(keyClass)
        ) {
            keyColumns = sqlStore.getKeyColumns();
            keySQLBuilder = this::toKeySQL;
            keysSQLBuilder = this::toKeysSQL;
        } else {
            keyColumns = SQLStoreColumnUtil.create(keyClass, sqlService);
            keySQLBuilder = this::toKeySQLByClass;
            keysSQLBuilder = this::toKeysSQLByClass;
        }
    }

    public <T> SQLStore<T> buildSQLStore(Class<T> clazz) {
        try {
            return SQLStoreFactory.build(sqlService, clazz);
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public long count() {
        Long count = sqlService.find(Long.class, sqlStore.getCount());
        return count == null ? 0 : count;
    }

    public long count(E entity) {
        try {
            Long count = sqlService.find(Long.class, SQLQueryUtil.setValue(sqlStore.getCountById(), sqlStore.getKeyColumns(), entity), sqlStore);
            return count == null ? 0 : count;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public long countById(K k) {
        try {
            Long count = sqlService.find(Long.class, keySQLBuilder.apply(sqlStore.getCountById(), k));
            return count == null ? 0 : count;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public List<E> findAll(Sort sort) {
        return sqlService.findAll(sqlStore, sqlStore.getSelect(), sort);
    }

    @Override
    public Page<E> page(Pagination pagination) {
        return sqlService.page(sqlStore, pagination);
    }

    public List<E> findAll() {
        return sqlService.findAll(sqlStore, sqlStore.getSelect());
    }

    public E findById(K k) {
        return sqlService.find(sqlStore, keySQLBuilder.apply(sqlStore.getSelectById(), k));
    }

    public final List<E> findAllByIds(K[] ks) {
        return sqlService.findAll(sqlStore, keysSQLBuilder.apply(sqlStore.getSelectByIds(), ks));
    }

    public E find(E e) {
        return sqlService.find(sqlStore, e);
    }

    public E insert(E e) {
        return sqlService.insert(sqlStore, e);
    }

    public int update(E e) {
        return sqlService.update(sqlStore, e);
    }

    public int delete(E e) {
        return sqlService.delete(sqlStore, e);
    }

    public int deleteById(K k) {
        return sqlService.update(keySQLBuilder.apply(sqlStore.getDeleteById(), k));
    }

    public int deleteByIds(K[] ks) {
        return sqlService.update(keysSQLBuilder.apply(sqlStore.getDeleteByIds(), ks));
    }

    @Override
    public Collection<E> insertAll(Collection<E> entities) {
        return sqlService.insertAll(sqlStore, entities);
    }

    @Override
    public E[] insertAll(E[] entities) {
        return sqlService.insertAll(sqlStore, entities);
    }

    @Override
    public int[] updateAll(Collection<E> entities) {
        return sqlService.updateAll(sqlStore, entities);
    }

    @Override
    public int[] updateAll(E[] entities) {
        return sqlService.updateAll(sqlStore, entities);
    }

    @Override
    public int deleteAll() {
        return sqlService.update(sqlStore.getDelete());
    }

    @Override
    public int[] deleteAll(Collection<E> entities) {
        return sqlService.deleteAll(sqlStore, entities);
    }

    @Override
    public int[] deleteAll(E[] entities) {
        return sqlService.deleteAll(sqlStore, entities);
    }


    @Override
    public <T> T findByObject(T object) {
        return sqlService.findByObject(object);
    }

    @Override
    public <T> T insertByObject(T object) {
        return sqlService.insertByObject(object);
    }

    @Override
    public <T> int updateByObject(T object) {
        return sqlService.updateByObject(object);
    }

    @Override
    public <T> int deleteByObject(T object) {
        return sqlService.deleteByObject(object);
    }


    protected String toKeySQL(SQLQueryBuilder builder, K k) {
        return SQLQueryUtil.setValue(builder, keyColumns, new Object[]{k});
    }

    protected String toKeySQLByClass(SQLQueryBuilder builder, K k) {
        return SQLQueryUtil.setValue(builder, keyColumns, k);
    }

    protected String toKeysSQL(SQLQueryBuilder builder, K[] values) {
        return builder.build().value("keys", values).toString();
    }

    public String toKeysSQLByClass(SQLQueryBuilder builder, K[] entities) {
        Object[][] array = new Object[entities.length][];
        int i = 0;
        for (K entity : entities) {
            Object[] row = array[i++] = new Object[keyColumns.length];
            int c = 0;
            for (SQLStoreColumn column : keyColumns) {
                try {
                    row[c++] = column.getValue(entity);
                } catch (IllegalAccessException e) {
                    throw new HiSqlException(e);
                }
            }
        }
        return builder.build().value("keys", array).toString();
    }
}

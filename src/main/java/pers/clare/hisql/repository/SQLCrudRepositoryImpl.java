package pers.clare.hisql.repository;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.KeySQLBuilder;
import pers.clare.hisql.function.KeysSQLBuilder;
import pers.clare.hisql.page.Next;
import pers.clare.hisql.page.Page;
import pers.clare.hisql.page.Pagination;
import pers.clare.hisql.page.Sort;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStoreFactory;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
public class SQLCrudRepositoryImpl<Entity, Key> extends SQLRepositoryImpl<SQLStoreService> implements SQLCrudRepository<Entity, Key> {
    protected final SQLCrudStore<Entity> sqlStore;
    protected final KeySQLBuilder<Key> keySQLBuilder;
    protected final KeysSQLBuilder<Key> keysSQLBuilder;

    @SuppressWarnings("unchecked")
    public SQLCrudRepositoryImpl(SQLStoreService sqlService, Class<Entity> repositoryClass) {
        super(sqlService);
        Type[] types = ClassUtil.findTypes(repositoryClass);
        Class<Entity> entityClass = (Class<Entity>) types[0];
        sqlStore = SQLStoreFactory.buildCrud(sqlService.getNaming(), sqlService.getResultSetConverter(), entityClass);

        Class<Key> keyClass = (Class<Key>) types[1];
        keySQLBuilder = SQLStoreFactory.buildKeySQLBuilder(keyClass, sqlStore);
        keysSQLBuilder = SQLStoreFactory.buildKeysSQLBuilder(keyClass, sqlStore);
    }

    public long count() {
        Long count = sqlService.find(Long.class, sqlStore.getCount());
        return count == null ? 0 : count;
    }

    public long count(Entity entity) {
        try {
            Long count = sqlService.find(Long.class, SQLQueryUtil.setValue(sqlStore.getCountById(), sqlStore.getKeyFields(), entity), sqlStore);
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

    public long countById(Boolean readonly, Key key) {
        try {
            Long count = sqlService.find(Long.class, keySQLBuilder.apply(sqlStore.getCountById(), key));
            return count == null ? 0 : count;
        } catch (HiSqlException e) {
            throw e;
        } catch (Exception e) {
            throw new HiSqlException(e);
        }
    }

    public List<Entity> findAll(Sort sort) {
        return sqlService.findAll(sqlStore, sqlStore.getSelect(), sort);
    }

    @Override
    public Page<Entity> page(Pagination pagination) {
        return sqlService.page(sqlStore, pagination);
    }

    @Override
    public Next<Entity> next(Pagination pagination) {
        return sqlService.next(sqlStore, pagination);
    }

    public List<Entity> findAll() {
        return sqlService.findAll(sqlStore, sqlStore.getSelect());
    }

    public Entity findById(Key key) {
        return sqlService.find(sqlStore, keySQLBuilder.apply(sqlStore.getSelectById(), key));
    }

    public final List<Entity> findAllByIds(Key[] keys) {
        return sqlService.findAll(sqlStore, keysSQLBuilder.apply(sqlStore.getSelectByIds(), keys));
    }

    public Entity find(Entity entity) {
        return sqlService.find(sqlStore, entity);
    }

    public Entity insert(Entity entity) {
        return sqlService.insert(sqlStore, entity);
    }

    public int update(Entity entity) {
        return sqlService.update(sqlStore, entity);
    }

    public int delete(Entity entity) {
        return sqlService.delete(sqlStore, entity);
    }

    public int deleteById(Key key) {
        return sqlService.update(keySQLBuilder.apply(sqlStore.getDeleteById(), key));
    }

    public int deleteByIds(Key[] keys) {
        return sqlService.update(keysSQLBuilder.apply(sqlStore.getDeleteByIds(), keys));
    }

    @Override
    public Collection<Entity> insertAll(Collection<Entity> entities) {
        return sqlService.insertAll(sqlStore, entities);
    }

    @Override
    public Entity[] insertAll(Entity[] entities) {
        return sqlService.insertAll(sqlStore, entities);
    }

    @Override
    public int[] updateAll(Collection<Entity> entities) {
        return sqlService.updateAll(sqlStore, entities);
    }

    @Override
    public int[] updateAll(Entity[] entities) {
        return sqlService.updateAll(sqlStore, entities);
    }

    @Override
    public int deleteAll() {
        return sqlService.update(sqlStore.getDelete());
    }

    @Override
    public int[] deleteAll(Collection<Entity> entities) {
        return sqlService.deleteAll(sqlStore, entities);
    }

    @Override
    public int[] deleteAll(Entity[] entities) {
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

}

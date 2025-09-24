package pers.clare.hisql.service;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreColumn;
import pers.clare.hisql.util.SQLStoreColumnUtil;
import pers.clare.hisql.util.SQLStoreUtil;

import javax.persistence.Table;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLStoreBasicService extends SQLService {

    static final Map<Class<?>, SQLStore<?>> storeCacheMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> SQLStore<T> build(
            Class<T> clazz
    ) {
        if (SQLStoreUtil.isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore.", clazz));
        return (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, this::doBuild);
    }

    @SuppressWarnings("unchecked")
    public <T> SQLCrudStore<T> buildCrud(
            Class<T> clazz
    ) {
        if (SQLStoreUtil.isIgnore(clazz))
            throw new Error(String.format("%s can not build SQLStore.", clazz));
        SQLStore<T> store = (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, this::doBuildCrud);
        if (!(store instanceof SQLCrudStore)) {
            store = this.doBuildCrud(clazz);
            storeCacheMap.putIfAbsent(clazz, store);
        }
        return (SQLCrudStore<T>) store;
    }

    private <T> SQLStore<T> doBuild(
            Class<T> clazz
    ) {
        try {
            return new SQLStore<>(clazz.getConstructor(), SQLStoreColumnUtil.create(clazz, this));
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private <T> SQLCrudStore<T> doBuildCrud(
            Class<T> clazz
    ) {
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            tableName = getNaming().turnCamelCase(clazz.getSimpleName());
        } else {
            tableName = table.name();
        }
        SQLStoreColumn[] columns = SQLStoreColumnUtil.create(clazz, this);
        int length = columns.length;
        int keyCount = 0;
        Field[] keyFields = new Field[length];
        Field autoKey = null;
        boolean ps = false;
        for (SQLStoreColumn column : columns) {
            if (column.isAuto()) autoKey = column.getField();
            if (column.isId()) {
                keyFields[keyCount++] = column.getField();
            }
            if (InputStream.class.isAssignableFrom(column.getField().getType())) {
                ps = true;
            }

        }
        Field[] temp = keyFields;
        keyFields = new Field[keyCount];
        System.arraycopy(temp, 0, keyFields, 0, keyCount);

        try {
            return new SQLCrudStore<>(clazz.getConstructor(), tableName, columns, autoKey, keyFields, ps);
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

}

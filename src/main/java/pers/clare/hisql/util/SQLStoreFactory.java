package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreColumn;

import javax.persistence.Table;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class SQLStoreFactory {
    private static final Map<Class<?>, SQLStore<?>> storeCacheMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> SQLStore<T> build(
            SQLBasicService service
            , Class<T> clazz
    ) {
        if (SQLStoreSqlUtil.isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore.", clazz));
        return (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, key -> doBuild(service, clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> SQLCrudStore<T> buildCrud(
            SQLBasicService service
            , Class<T> clazz
    ) {
        if (SQLStoreSqlUtil.isIgnore(clazz))
            throw new Error(String.format("%s can not build SQLStore.", clazz));
        SQLStore<T> store = (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, key -> doBuildCrud(service, clazz));
        if (!(store instanceof SQLCrudStore)) {
            store = doBuildCrud(service, clazz);
            storeCacheMap.putIfAbsent(clazz, store);
        }
        return (SQLCrudStore<T>) store;
    }

    private static <T> SQLStore<T> doBuild(
            SQLBasicService service
            , Class<T> clazz
    ) {
        try {
            return new SQLStore<>(clazz.getConstructor(), SQLStoreColumnUtil.create(clazz, service));
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private static <T> SQLCrudStore<T> doBuildCrud(
            SQLBasicService service
            , Class<T> clazz
    ) {
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            tableName = service.getNaming().turnCamelCase(clazz.getSimpleName());
        } else {
            tableName = table.name();
        }
        SQLStoreColumn[] columns = SQLStoreColumnUtil.create(clazz, service);
        int keyCount = 0;
        Field[] keyFields = new Field[columns.length];
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

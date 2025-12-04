package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.store.SQLCrudStore;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreColumn;

import javax.persistence.Table;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
        validate(clazz);
        return (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, key -> doBuild(service, clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> SQLCrudStore<T> buildCrud(
            SQLBasicService service
            , Class<T> clazz
    ) {
        validate(clazz);
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
        List<SQLStoreColumn> keyColumns = new ArrayList<>(columns.length);
        SQLStoreColumn autoKey = null;
        boolean ps = false;
        for (SQLStoreColumn column : columns) {
            if (column.isAuto()) autoKey = column;
            if (column.isId()) {
                keyColumns.add(column);
            }
            if (InputStream.class.isAssignableFrom(column.getType())) {
                ps = true;
            }
        }

        try {
            return new SQLCrudStore<>(clazz.getConstructor(), tableName, columns, autoKey, keyColumns.toArray(new SQLStoreColumn[0]), ps);
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private void validate(Class<?> clazz) {
        if (ClassUtil.isIgnore(clazz)) {
            throw new HiSqlException(String.format("%s can not build SQLStore.", clazz));
        }
    }
}

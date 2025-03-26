package pers.clare.hisql.store;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.FieldSetter;
import pers.clare.hisql.function.KeySQLBuilder;
import pers.clare.hisql.function.KeysSQLBuilder;
import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.FieldColumnFactory;
import pers.clare.hisql.util.SQLQueryUtil;

import javax.persistence.Table;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class SQLStoreFactory {

    static final Map<Class<?>, SQLStore<?>> storeCacheMap = new ConcurrentHashMap<>();

    static final Map<ResultSetConverter, Map<Class<?>, Map<String, FieldSetter>>> converterFieldSetMap = new ConcurrentHashMap<>();

    static final Map<Class<?>, Field[]> keyFieldsMap = new ConcurrentHashMap<>();


    public static <T> KeySQLBuilder<T> buildKeySQLBuilder(Class<T> keyClass, SQLCrudStore<?> store) {
        if (ClassUtil.isBasicType(keyClass)
            || ClassUtil.isBasicTypeArray(keyClass)
        ) {
            return (builder, key) -> SQLQueryUtil.setValue(builder, store.getKeyFields(), new Object[]{key});
        } else {
            Field[] keyFields = buildKeyFields(keyClass, store);
            return (builder, key) -> SQLQueryUtil.setValue(builder, keyFields, key);
        }
    }

    public static <T> KeysSQLBuilder<T> buildKeysSQLBuilder(Class<T> keyClass, SQLCrudStore<?> store) {
        if (ClassUtil.isBasicType(keyClass)
            || ClassUtil.isBasicTypeArray(keyClass)
        ) {
            return SQLStoreFactory::toKeysSQL;
        } else {
            Field[] keyFields = buildKeyFields(keyClass, store);
            return SQLStoreFactory::toKeysSQLByClass;
        }
    }

    public static <T> String toKeysSQL(SQLQueryBuilder builder, T[] values) {
        return builder.build().value("keys", values).toString();
    }

    public static <T> String toKeysSQLByClass(SQLQueryBuilder builder, T[] values) {
        Class<?> keyClass = values.getClass().getComponentType();
        Field[] keyFields = keyFieldsMap.get(keyClass);
        Object[][] array = new Object[values.length][];
        int i = 0;
        for (T value : values) {
            Object[] row = array[i++] = new Object[keyFields.length];
            int c = 0;
            for (Field field : keyFields) {
                try {
                    row[c++] = field.get(value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return builder.build().value("keys", array).toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> SQLStore<T> build(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        if (FieldColumnFactory.isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore", clazz));
        return (SQLStore<T>) storeCacheMap.computeIfAbsent(clazz, (key) -> doBuild(naming, converter, clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T> SQLCrudStore<T> buildCrud(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        if (FieldColumnFactory.isIgnore(clazz))
            throw new Error(String.format("%s can not build SQLStore.", clazz));
        SQLStore<T> store = (SQLStore<T>) storeCacheMap.get(clazz);
        if (!(store instanceof SQLCrudStore)) {
            store = doBuildCrud(naming, converter, clazz);
            storeCacheMap.put(clazz, store);
        }
        return (SQLCrudStore<T>) store;
    }

    private static <T> SQLStore<T> doBuild(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        try {
            return new SQLStore<>(clazz.getConstructor(), buildFieldSetters(naming, clazz, converter));
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private static <T> SQLCrudStore<T> doBuildCrud(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            tableName = naming.turnCamelCase(clazz.getSimpleName());
        } else {
            tableName = table.name();
        }
        FieldColumn[] fieldColumns = FieldColumnFactory.get(naming, clazz);
        int length = fieldColumns.length;
        int keyCount = 0;
        Field[] keyFields = new Field[length];
        Field autoKey = null;
        boolean ps = false;
        for (FieldColumn column : fieldColumns) {
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
            return new SQLCrudStore<>(clazz.getConstructor()
                    , buildFieldSetters(naming, clazz, converter)
                    , tableName, fieldColumns, autoKey, keyFields, ps
            );
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private static Map<String, FieldSetter> buildFieldSetters(NamingStrategy naming, Class<?> clazz, ResultSetConverter converter) {
        return converterFieldSetMap.computeIfAbsent(converter, (c) -> new ConcurrentHashMap<>()).computeIfAbsent(clazz, (key) -> {
            FieldColumn[] fields = FieldColumnFactory.get(naming, clazz);
            Map<String, FieldSetter> fieldSetMap = new ConcurrentHashMap<>();
            for (FieldColumn fieldColumn : fields) {
                Field field = fieldColumn.getField();
                String name = fieldColumn.getColumnName().replaceAll("`", "");
                FieldSetter fieldSetter = buildFieldSetter(converter, field);
                fieldSetMap.put(field.getName(), fieldSetter);
                fieldSetMap.put(name, fieldSetter);
                fieldSetMap.put(name.toUpperCase(), fieldSetter);
            }
            return fieldSetMap;
        });
    }

    private static FieldSetter buildFieldSetter(ResultSetConverter resultSetConverter, Field field) {
        ResultSetValueConverter<?> valueConverter = resultSetConverter.get(field.getType());
        if (valueConverter == null) {
            if (field.getType() == Object.class) {
                return (target, rs, index) -> field.set(target, rs.getObject(index));
            } else {
                return (target, rs, index) -> field.set(target, rs.getObject(index, field.getType()));
            }
        } else {
            return (target, rs, index) -> field.set(target, valueConverter.apply(rs, index));
        }
    }

    private static Field[] buildKeyFields(Class<?> keyClass, SQLCrudStore<?> store) {
        return keyFieldsMap.computeIfAbsent(keyClass, (clazz) -> {
            Field[] storeKeyFields = store.getKeyFields();
            Field[] fields = new Field[storeKeyFields.length];
            int count = 0;
            for (Field keyField : storeKeyFields) {
                try {
                    Field field = fields[count++] = clazz.getDeclaredField(keyField.getName());
                    field.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(String.format("%s %s field not found!", clazz, keyField.getName()));
                }
            }
            return fields;
        });
    }

    private static char[] merge(String... strings) {
        int size = 0;
        for (String string : strings) {
            size += string.length();
        }
        char[] cs = new char[size];
        int i = 0;
        for (String string : strings) {
            string.getChars(0, string.length(), cs, i);
            i += string.length();
        }
        return cs;
    }

}

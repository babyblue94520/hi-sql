package pers.clare.hisql.store;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.FieldSetter;
import pers.clare.hisql.function.KeySQLBuilder;
import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.FieldColumnFactory;
import pers.clare.hisql.util.SQLQueryUtil;

import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class SQLStoreFactory {

    static final Map<Class<?>, SQLStore<?>> sqlStoreCacheMap = new ConcurrentHashMap<>();
    static final Map<ResultSetConverter, Map<Class<?>, Map<String, FieldSetter>>> converterFieldSetMap = new ConcurrentHashMap<>();

    public static <T> KeySQLBuilder<T> buildKey(Class<T> keyClass, SQLCrudStore<?> sqlStore) {
        if (ClassUtil.isBasicType(keyClass)
            || keyClass.isArray()
        ) {
            return (builder, key) -> SQLQueryUtil.setValue(builder, sqlStore.getKeyFields(), new Object[]{key});
        } else {
            Field[] keyFields = new Field[sqlStore.getKeyFields().length];
            int count = 0;
            for (Field field : sqlStore.getKeyFields()) {
                try {
                    Field keyField = keyFields[count++] = keyClass.getDeclaredField(field.getName());
                    keyField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(String.format("%s %s field not found!", keyClass, field.getName()));
                }
            }
            return (builder, key) -> SQLQueryUtil.setValue(builder, keyFields, key);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> SQLStore<T> build(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        if (FieldColumnFactory.isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore", clazz));
        return (SQLStore<T>) sqlStoreCacheMap.computeIfAbsent(clazz, (key) -> doBuild(naming, converter, clazz));
    }


    @SuppressWarnings("unchecked")
    public static <T> SQLCrudStore<T> buildCrud(
            NamingStrategy naming
            , ResultSetConverter converter
            , Class<T> clazz
    ) {
        if (FieldColumnFactory.isIgnore(clazz)) throw new Error(String.format("%s can not build SQLCrudStore", clazz));
        SQLStore<T> store = (SQLStore<T>) sqlStoreCacheMap.get(clazz);
        if (store instanceof SQLCrudStore) return (SQLCrudStore<T>) store;
        store = doBuildCrud(naming, converter, clazz);
        sqlStoreCacheMap.put(clazz, store);
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
        int fieldColumnCount = 0;
        Field[] keyFields = new Field[length];
        StringBuilder selectColumns = new StringBuilder();
        StringBuilder whereId = new StringBuilder(" where ");
        Field autoKey = null;
        boolean nullable, insertable, updatable;
        for (FieldColumn column : fieldColumns) {
            String columnName = column.getColumnName();

            if (column.isAuto()) autoKey = column.getField();

            if (column.isId()) {
                keyFields[keyCount++] = column.getField();
                whereId.append(columnName)
                        .append('=')
                        .append(':')
                        .append(column.getField().getName())
                        .append(" and ");
            }
            selectColumns.append(columnName).append(',');
        }
        Field[] temp = keyFields;
        keyFields = new Field[keyCount];
        System.arraycopy(temp, 0, keyFields, 0, keyCount);
        selectColumns.delete(selectColumns.length() - 1, selectColumns.length());
        whereId.delete(whereId.length() - 5, whereId.length());

        try {
            return new SQLCrudStore<>(clazz.getConstructor()
                    , buildFieldSetters(naming, clazz, converter)
                    , tableName, fieldColumns, autoKey, keyFields
                    , buildCount(tableName)
                    , buildCountById(tableName, whereId)
                    , buildSelect(tableName, selectColumns)
                    , buildSelectById(tableName, selectColumns, whereId)
                    , buildDeleteAll(tableName)
                    , buildDeleteById(tableName, whereId)
            );
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }


    private static String buildCount(String tableName) {
        int tl = tableName.length();
        char[] chars = new char[21 + tl];
        int index = 0;
        "select count(*) from ".getChars(0, 21, chars, index);
        index += 21;
        tableName.getChars(0, tl, chars, index);
        return new String(chars);
    }

    private static SQLQueryBuilder buildCountById(String tableName, StringBuilder whereId) {
        int tl = tableName.length();
        int wl = whereId.length();
        char[] chars = new char[21 + tl + wl];
        int index = 0;
        "select count(*) from ".getChars(0, 21, chars, index);
        index += 21;
        tableName.getChars(0, tl, chars, index);
        index += tl;
        whereId.getChars(0, wl, chars, index);
        return SQLQueryBuilder.create(chars);
    }

    private static String buildSelect(String tableName, StringBuilder selectColumns) {
        int tl = tableName.length();
        int scl = selectColumns.length();

        char[] chars = new char[13 + tl + scl];
        int index = 0;
        "select ".getChars(0, 7, chars, index);
        index += 7;
        selectColumns.getChars(0, scl, chars, index);
        index += scl;
        " from ".getChars(0, 6, chars, index);
        index += 6;
        tableName.getChars(0, tl, chars, index);
        return new String(chars);
    }

    private static SQLQueryBuilder buildSelectById(String tableName, StringBuilder selectColumns, StringBuilder whereId) {
        int tl = tableName.length();
        int scl = selectColumns.length();
        int wl = whereId.length();

        char[] chars = new char[13 + tl + scl + wl];
        int index = 0;
        "select ".getChars(0, 7, chars, index);
        index += 7;
        selectColumns.getChars(0, scl, chars, index);
        index += scl;
        " from ".getChars(0, 6, chars, index);
        index += 6;
        tableName.getChars(0, tl, chars, index);
        index += tl;
        whereId.getChars(0, wl, chars, index);
        return SQLQueryBuilder.create(chars);
    }

    private static String buildDeleteAll(String tableName) {
        int tl = tableName.length();
        char[] chars = new char[12 + tl];
        int index = 0;
        "delete from ".getChars(0, 12, chars, index);
        index += 12;
        tableName.getChars(0, tl, chars, index);
        return new String(chars);
    }

    private static SQLQueryBuilder buildDeleteById(String tableName, StringBuilder whereId) {
        int tl = tableName.length();
        int wl = whereId.length();
        char[] chars = new char[12 + tl + wl];
        int index = 0;
        "delete from ".getChars(0, 12, chars, index);
        index += 12;
        tableName.getChars(0, tl, chars, index);
        index += tl;
        whereId.getChars(0, wl, chars, index);
        return SQLQueryBuilder.create(chars);
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


}

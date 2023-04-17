package pers.clare.hisql.store;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.FieldSetHandler;
import pers.clare.hisql.function.KeySQLBuilder;
import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.query.SQLQueryBuilder;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.support.ResultSetConverter;
import pers.clare.hisql.util.ClassUtil;
import pers.clare.hisql.util.SQLQueryUtil;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class SQLStoreFactory {

    static final Map<Class<?>, SQLStore<?>> sqlStoreCacheMap = new ConcurrentHashMap<>();

    public static <T> SQLStore<T> find(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        SQLStore<T> store = (SQLStore<T>) sqlStoreCacheMap.get(clazz);
        if (store == null) {
            throw new HiSqlException("%s have not build SQLStore", clazz.getName());
        }
        return store;
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz == null
                || clazz.isPrimitive()
                || clazz.getName().startsWith("java.")
                || clazz.isArray()
                || Collection.class.isAssignableFrom(clazz)
                || clazz.isEnum()
                || clazz.isInterface()
                ;
    }

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
    public static <R extends SQLStore<T>, T> R build(SQLBasicService service, Class<T> clazz, boolean crud) {
        if (isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore", clazz));
        SQLStore<T> store = (SQLStore<T>) sqlStoreCacheMap.get(clazz);
        if (crud && store instanceof SQLCrudStore) return (R) store;
        store = crud ? buildCrud(service, clazz) : build(service, clazz);
        sqlStoreCacheMap.put(clazz, store);
        return (R) store;
    }

    private static <T> SQLStore<T> build(SQLBasicService service, Class<T> clazz) {
        Collection<Field> fields = getAllField(clazz);
        Map<String, FieldSetHandler> fieldSetMap = new HashMap<>();
        String name;
        FieldSetHandler fieldSetHandler;
        for (Field field : fields) {
            field.setAccessible(true);
            name = getColumnName(service.getNaming(), field, field.getAnnotation(Column.class)).replaceAll("`", "");
            fieldSetHandler = buildSetHandler(service.getResultSetConverter(), field);
            fieldSetMap.put(field.getName(), fieldSetHandler);
            fieldSetMap.put(name, fieldSetHandler);
            fieldSetMap.put(name.toUpperCase(), fieldSetHandler);
        }
        try {
            return new SQLStore<>(clazz.getConstructor(), fieldSetMap);
        } catch (NoSuchMethodException e) {
            throw new HiSqlException(e.getMessage());
        }
    }

    private static <T> SQLCrudStore<T> buildCrud(SQLBasicService service, Class<T> clazz) {
        String tableName;
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            tableName = service.getNaming().turnCamelCase(clazz.getSimpleName());
        } else {
            tableName = table.name();
        }
        StringBuilder selectColumns = new StringBuilder();
        StringBuilder whereId = new StringBuilder(" where ");
        Collection<Field> fields = getAllField(clazz);
        int length = fields.size();
        int keyCount = 0;
        int fieldColumnCount = 0;
        Map<String, FieldSetHandler> fieldSetMap = new HashMap<>(length * 3);
        Field[] keyFields = new Field[length];
        FieldColumn[] fieldColumns = new FieldColumn[length];
        Column column;
        String columnName, fieldName, name;
        boolean id, auto;
        Field autoKey = null;
        FieldSetHandler fieldSetHandler;
        boolean nullable, insertable, updatable;
        for (Field field : fields) {
            field.setAccessible(true);
            column = field.getAnnotation(Column.class);
            fieldName = field.getName();
            columnName = getColumnName(service.getNaming(), field, column);
            name = columnName.replaceAll("`", "");

            fieldSetHandler = buildSetHandler(service.getResultSetConverter(), field);
            fieldSetMap.put(fieldName, fieldSetHandler);
            fieldSetMap.put(name, fieldSetHandler);
            fieldSetMap.put(name.toUpperCase(), fieldSetHandler);

            id = field.getAnnotation(Id.class) != null;
            auto = field.getAnnotation(GeneratedValue.class) != null;

            if (auto) autoKey = field;
            if (column == null) {
                nullable = insertable = updatable = true;
            } else {
                nullable = column.nullable();
                insertable = column.insertable();
                updatable = column.updatable();
            }
            fieldColumns[fieldColumnCount++] = new FieldColumn(field, id, auto, !nullable, insertable, updatable, columnName);

            if (id) {
                keyFields[keyCount++] = field;
                whereId.append(columnName)
                        .append('=')
                        .append(':')
                        .append(fieldName)
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
            return new SQLCrudStore<>(clazz.getConstructor(), fieldSetMap, tableName, fieldColumns, autoKey, keyFields
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

    private static Collection<Field> getAllField(Class<?> clazz) {
        Map<String, Integer> orderMap = new HashMap<>();
        List<Field> result = new ArrayList<>();
        if (!isIgnore(clazz)) {
            putAllField(clazz, orderMap, result);
        }
        return result;
    }

    private static void putAllField(Class<?> clazz, Map<String, Integer> orderMap, List<Field> result) {
        if (isIgnore(clazz)) return;
        putAllField(clazz.getSuperclass(), orderMap, result);
        putAllField(clazz.getDeclaredFields(), orderMap, result);
    }

    private static void putAllField(Field[] fields, Map<String, Integer> orderMap, List<Field> result) {
        int modifier;
        String name;
        Integer index;
        for (Field field : fields) {
            modifier = field.getModifiers();
            if (Modifier.isStatic(modifier)
                    || Modifier.isFinal(modifier)
            ) continue;

            boolean transientField = field.getAnnotation(Transient.class) != null;

            name = field.getName();
            index = orderMap.get(name);
            if (index == null) {
                if (transientField) return;
                orderMap.put(name, result.size());
                result.add(field);
            } else {
                if (transientField) {
                    result.remove((int) index);
                    Iterator<Map.Entry<String, Integer>> iterator = orderMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        Integer value = entry.getValue();
                        if (Objects.equals(value, index)) {
                            iterator.remove();
                        } else if (value > index) {
                            entry.setValue(value - 1);
                        }
                    }
                    orderMap.remove(name);
                } else {
                    result.set(index, field);
                }
            }
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

    private static FieldSetHandler buildSetHandler(ResultSetConverter resultSetConverter, Field field) {
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

    private static String getColumnName(NamingStrategy namingStrategy, Field field, Column column) {
        return column == null || column.name().length() == 0 ? namingStrategy.turnCamelCase(field.getName()) : column.name();
    }
}

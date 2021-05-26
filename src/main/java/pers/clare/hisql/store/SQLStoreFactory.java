package pers.clare.hisql.store;

import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.function.FieldSetHandler;
import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.query.SQLQueryBuilder;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class SQLStoreFactory {

    static Map<Class<?>, SQLStore<?>> sqlStoreCacheMap = new ConcurrentHashMap<>();

    public static <T> SQLStore<T> find(Class<T> clazz) {
        SQLStore<T> store = (SQLStore<T>) sqlStoreCacheMap.get(clazz);
        if (store == null) {
            throw new HiSqlException("%s have not build SQLStore", clazz.getName());
        }
        return store;
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz == null
                || clazz.isPrimitive()
                || clazz.getName().startsWith("java.lang")
                || clazz.isArray()
                || Collection.class.isAssignableFrom(clazz)
                || clazz.isEnum()
                || clazz.isInterface()
                ;
    }

    public static <T> SQLStore<T> build(HiSqlContext context, Class<T> clazz, boolean crud) {
        if (isIgnore(clazz)) throw new Error(String.format("%s can not build SQLStore", clazz));
        SQLStore<T> store = (SQLStore<T>) sqlStoreCacheMap.get(clazz);
        if (crud && store instanceof SQLCrudStore) return store;
        store = crud ? buildCrud(context, clazz) : build(context, clazz);
        sqlStoreCacheMap.put(clazz, store);
        return store;
    }

    private static <T> SQLStore<T> build(HiSqlContext context, Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, FieldSetHandler> fieldSetMap = new HashMap<>(fields.length * 3);
        String name;
        FieldSetHandler fieldSetHandler;
        int modifier;
        for (Field field : fields) {
            modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) continue;
            field.setAccessible(true);
            name = getColumnName(context, field, field.getAnnotation(Column.class)).replaceAll("`", "");
            fieldSetHandler = buildSetHandler(field);
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

    private static <T> SQLCrudStore<T> buildCrud(HiSqlContext context, Class<T> clazz) {
        String tableName = context.getNaming().turnCamelCase(clazz.getSimpleName());
        StringBuilder selectColumns = new StringBuilder();
        StringBuilder whereId = new StringBuilder(" where ");
        Field[] fields = clazz.getDeclaredFields();
        int length = fields.length;
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
        int modifier;
        for (Field field : fields) {
            modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isFinal(modifier)) continue;
            field.setAccessible(true);
            column = field.getAnnotation(Column.class);
            fieldName = field.getName();
            columnName = getColumnName(context, field, column).replaceAll("`", "");
            name = columnName.replaceAll("`", "");

            fieldSetHandler = buildSetHandler(field);
            fieldSetMap.put(fieldName, fieldSetHandler);
            fieldSetMap.put(name, fieldSetHandler);
            fieldSetMap.put(name.toUpperCase(), fieldSetHandler);

            if (field.getAnnotation(Transient.class) != null) continue;
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
            fieldColumns[fieldColumnCount++] = new FieldColumn(field, id, auto, nullable, insertable, updatable, columnName);

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
        return new SQLQueryBuilder(chars);
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
        return new SQLQueryBuilder(chars);
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
        return new SQLQueryBuilder(chars);
    }

    private static FieldSetHandler buildSetHandler(Field field) {
        ResultSetValueConverter<?> resultSetValueConverter = HiSqlContext.getResultSetValueConverter(field.getType());
        if (resultSetValueConverter == null) {
            if (field.getType() == Object.class) {
                return (target, rs, index) -> field.set(target, rs.getObject(index));
            } else {
                return (target, rs, index) -> field.set(target, rs.getObject(index, field.getType()));
            }
        } else {
            return (target, rs, index) -> field.set(target, resultSetValueConverter.apply(rs.getObject(index)));
        }
    }

    private static String getColumnName(HiSqlContext context, Field field, Column column) {
        return column == null || column.name().length() == 0 ? context.getNaming().turnCamelCase(field.getName()) : column.name();
    }
}

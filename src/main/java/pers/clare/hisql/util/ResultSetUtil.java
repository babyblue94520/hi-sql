package pers.clare.hisql.util;

import pers.clare.hisql.function.FieldSetter;
import pers.clare.hisql.function.ResultSetValueConverter;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.support.ResultSetConverter;

import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ResultSetUtil {

    private ResultSetUtil() {
    }

    public static String[] getNames(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        int i;
        String[] names = new String[count];
        for (i = 0; i < count; ) {
            names[i] = metaData.getColumnLabel(++i);
        }
        return names;
    }

    public static <T> T to(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> clazz) throws SQLException {
        if (rs.next()) {
            return getValue(resultSetConverter, rs, 1, clazz);
        }
        return null;
    }

    public static <T> Map<String, T> toMap(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> valueClass) throws SQLException {
        if (rs.next()) {
            return toMap(resultSetConverter, rs, valueClass, getNames(rs));
        }
        return null;
    }

    public static <T> Set<T> toSet(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> clazz) throws SQLException {
        Set<T> result = new HashSet<>();
        while (rs.next()) {
            result.add(getValue(resultSetConverter, rs, 1, clazz));
        }
        return result;
    }

    public static <T> Set<Map<String, T>> toMapSet(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> valueClass) throws SQLException {
        return toMapCollection(resultSetConverter, rs, valueClass, new HashSet<>());
    }

    public static <T> List<Map<String, T>> toMapList(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> valueClass) throws SQLException {
        return toMapCollection(resultSetConverter, rs, valueClass, new ArrayList<>());
    }

    public static <T> List<T> toList(ResultSetConverter resultSetConverter, ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(getValue(resultSetConverter, rs, 1, clazz));
        }
        return result;
    }

    public static <T> T toInstance(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        FieldSetter[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetterMap());
        if (rs.next()) {
            return buildInstance(rs, sqlStore.getConstructor(), fields);
        }
        return null;
    }

    public static <T> Set<T> toSetInstance(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        Set<T> result = new HashSet<>();
        FieldSetter[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetterMap());
        while (rs.next()) {
            result.add(buildInstance(rs, sqlStore.getConstructor(), fields));
        }
        return result;
    }

    public static <T> List<T> toInstances(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        List<T> list = new ArrayList<>();
        FieldSetter[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetterMap());
        while (rs.next()) {
            list.add(buildInstance(rs, sqlStore.getConstructor(), fields));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> toMap(
            ResultSetConverter resultSetConverter
            , ResultSet rs
            , Class<T> valueClass
            , String[] names
    ) throws SQLException {
        Map<String, T> map = new HashMap<>(names.length);
        int i = 1;
        if (valueClass == Object.class) {
            for (String name : names) {
                map.put(name, (T) rs.getObject(i++));
            }
        } else {
            for (String name : names) {
                map.put(name, getValue(resultSetConverter, rs, i++, valueClass));
            }
        }
        return map;
    }

    private static <T, C extends Collection<Map<String, T>>> C toMapCollection(
            ResultSetConverter resultSetConverter
            , ResultSet rs
            , Class<T> valueClass
            , C collection
    ) throws SQLException {
        String[] names = getNames(rs);
        while (rs.next()) {
            collection.add(toMap(resultSetConverter, rs, valueClass, names));
        }
        return collection;
    }

    private static <T> T buildInstance(ResultSet rs, Constructor<T> constructor, FieldSetter[] fields) throws Exception {
        T target = constructor.newInstance();
        int i = 1;
        for (FieldSetter field : fields) {
            if (field != null) field.apply(target, rs, i);
            i++;
        }
        return target;
    }

    private static FieldSetter[] toFields(ResultSetMetaData metaData, Map<String, FieldSetter> fieldMap) throws Exception {
        int l = metaData.getColumnCount();
        FieldSetter[] fields = new FieldSetter[l];
        for (int i = 0; i < l; i++) {
            fields[i] = fieldMap.get(metaData.getColumnLabel(i + 1));
        }
        return fields;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(ResultSetConverter resultSetConverter, ResultSet rs, int index, Class<T> clazz) throws SQLException {
        ResultSetValueConverter<T> valueConverter = resultSetConverter.get(clazz);
        if (valueConverter == null) {
            if (clazz == Object.class) {
                return (T) rs.getObject(index);
            } else {
                return rs.getObject(index, clazz);
            }
        } else {
            return valueConverter.apply(rs, index);
        }
    }
}

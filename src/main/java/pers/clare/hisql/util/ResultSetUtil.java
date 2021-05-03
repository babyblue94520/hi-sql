package pers.clare.hisql.util;

import pers.clare.hisql.function.FieldSetHandler;
import pers.clare.hisql.store.SQLStore;

import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;

public class ResultSetUtil {

    private ResultSetUtil(){}

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

    public static <T> T to(Class<T> clazz, ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getObject(1, clazz);
        }
        return null;
    }

    public static <T> Map<String, T> toMap(Class<T> valueClass, ResultSet rs) throws SQLException {
        if (rs.next()) {
            if (valueClass == Object.class) {
                return (Map<String, T>) toMap(rs, getNames(rs));
            } else {
                return toMap(valueClass, rs, getNames(rs));
            }
        }
        return null;
    }

    private static Map<String, Object> toMap(ResultSet rs, String[] names) throws SQLException {
        Map<String, Object> map = new HashMap<>(names.length);
        int i = 1;
        for (String name : names) {
            map.put(name, rs.getObject(i++));
        }
        return map;
    }

    private static <T> Map<String, T> toMap(Class<T> valueClass, ResultSet rs, String[] names) throws SQLException {
        Map<String, T> map = new HashMap<>(names.length);
        int i = 1;
        for (String name : names) {
            map.put(name, rs.getObject(i++, valueClass));
        }
        return map;
    }

    public static <T> Set<T> toSet(Class<T> clazz, ResultSet rs) throws SQLException {
        Set<T> result = new HashSet<>();
        while (rs.next()) {
            result.add(rs.getObject(1, clazz));
        }
        return result;
    }

    public static <T> Set<Map<String, T>> toMapSet(Class<T> valueClass, ResultSet rs) throws SQLException {
        return (Set<Map<String, T>>) toMapCollection(valueClass, rs, new HashSet<>());
    }

    public static <T> List<Map<String, T>> toMapList(Class<T> valueClass, ResultSet rs) throws SQLException {
        return (List<Map<String, T>>) toMapCollection(valueClass, rs, new ArrayList<>());
    }

    private static <T> Collection<Map<String, T>> toMapCollection(Class<T> valueClass, ResultSet rs, Collection<Map<String, T>> collection) throws SQLException {
        String[] names = getNames(rs);
        if (valueClass == Object.class) {
            while (rs.next()) {
                collection.add((Map<String, T>) toMap(rs, names));
            }
        } else {
            while (rs.next()) {
                collection.add(toMap(valueClass, rs, names));
            }
        }
        return collection;
    }

    public static <T> List<T> toList(Class<T> clazz, ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getObject(1, clazz));
        }
        return result;
    }


    public static <T> T toInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        if (rs.next()) {
            return buildInstance(sqlStore.getConstructor(), fields, rs);
        }
        return null;
    }

    public static <T> Set<T> toSetInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        Set<T> result = new HashSet<>();
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        while (rs.next()) {
            result.add(buildInstance(sqlStore.getConstructor(), fields, rs));
        }
        return result;
    }

    public static <T> List<T> toInstances(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        List<T> list = new ArrayList<>();
        FieldSetHandler[] fields = toFields(sqlStore.getFieldSetMap(), rs.getMetaData());
        while (rs.next()) {
            list.add(buildInstance(sqlStore.getConstructor(), fields, rs));
        }
        return list;
    }

    private static <T> T buildInstance(Constructor<T> constructor, FieldSetHandler[] fields, ResultSet rs) throws Exception {
        T target = constructor.newInstance();
        int i = 1;
        for (FieldSetHandler field : fields) {
            if (field == null) continue;
            field.apply(target, rs, i++);
        }
        return target;
    }

    private static FieldSetHandler[] toFields(Map<String, FieldSetHandler> fieldMap, ResultSetMetaData metaData) throws Exception {
        int l = metaData.getColumnCount();
        FieldSetHandler[] fields = new FieldSetHandler[l];
        for (int i = 0; i < l; i++) {
            fields[i] = fieldMap.get(metaData.getColumnLabel(i + 1));
        }
        return fields;
    }
}

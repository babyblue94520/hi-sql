package pers.clare.hisql.util;

import pers.clare.hisql.function.FieldSetHandler;
import pers.clare.hisql.function.ResultSetHandler;
import pers.clare.hisql.function.StoreResultSetHandler;
import pers.clare.hisql.store.SQLStore;

import java.lang.reflect.Constructor;
import java.sql.*;
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

    public static ResultSetHandler<?, ?> to = ResultSetUtil::to;

    public static <T> T to(ResultSet rs, Class<T> clazz) throws SQLException {
        if (rs.next()) {
            if(Blob.class.isAssignableFrom(clazz)){
                return (T) rs.getBlob(1);
            }else{
                return rs.getObject(1, clazz);
            }
        }
        return null;
    }

    public static ResultSetHandler<?, ?> toMap = ResultSetUtil::toMap;

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> toMap(ResultSet rs, Class<T> valueClass) throws SQLException {
        if (rs.next()) {
            if (valueClass == Object.class) {
                return (Map<String, T>) toMap(rs, getNames(rs));
            } else {
                return toMap(rs, valueClass, getNames(rs));
            }
        }
        return null;
    }

    public static ResultSetHandler<?, ?> toSet = ResultSetUtil::toSet;

    public static <T> Set<T> toSet(ResultSet rs, Class<T> clazz) throws SQLException {
        Set<T> result = new HashSet<>();
        while (rs.next()) {
            result.add(rs.getObject(1, clazz));
        }
        return result;
    }

    public static ResultSetHandler<?, ?> toMapSet = ResultSetUtil::toMapSet;

    public static <T> Set<Map<String, T>> toMapSet(ResultSet rs, Class<T> valueClass) throws SQLException {
        return (Set<Map<String, T>>) toMapCollection(rs, valueClass, new HashSet<>());
    }

    public static ResultSetHandler<?, ?> toMapList = ResultSetUtil::toMapList;

    public static <T> List<Map<String, T>> toMapList(ResultSet rs, Class<T> valueClass) throws SQLException {
        return (List<Map<String, T>>) toMapCollection(rs, valueClass, new ArrayList<>());
    }

    public static ResultSetHandler<?, ?> toList = ResultSetUtil::toList;

    public static <T> List<T> toList(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getObject(1, clazz));
        }
        return result;
    }

    public static StoreResultSetHandler<?, ?> toInstance = ResultSetUtil::toInstance;

    public static <T> T toInstance(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        FieldSetHandler[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetMap());
        if (rs.next()) {
            return buildInstance(rs, sqlStore.getConstructor(), fields);
        }
        return null;
    }

    public static StoreResultSetHandler<?, ?> toSetInstance = ResultSetUtil::toSetInstance;

    public static <T> Set<T> toSetInstance(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        Set<T> result = new HashSet<>();
        FieldSetHandler[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetMap());
        while (rs.next()) {
            result.add(buildInstance(rs, sqlStore.getConstructor(), fields));
        }
        return result;
    }

    public static StoreResultSetHandler<?, ?> toInstances = ResultSetUtil::toInstances;

    public static <T> List<T> toInstances(ResultSet rs, SQLStore<T> sqlStore) throws Exception {
        List<T> list = new ArrayList<>();
        FieldSetHandler[] fields = toFields(rs.getMetaData(), sqlStore.getFieldSetMap());
        while (rs.next()) {
            list.add(buildInstance(rs, sqlStore.getConstructor(), fields));
        }
        return list;
    }

    private static Map<String, Object> toMap(ResultSet rs, String[] names) throws SQLException {
        Map<String, Object> map = new HashMap<>(names.length);
        int i = 1;
        for (String name : names) {
            map.put(name, rs.getObject(i++));
        }
        return map;
    }

    private static <T> Map<String, T> toMap(ResultSet rs, Class<T> valueClass, String[] names) throws SQLException {
        Map<String, T> map = new HashMap<>(names.length);
        int i = 1;
        for (String name : names) {
            map.put(name, rs.getObject(i++, valueClass));
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <T> Collection<Map<String, T>> toMapCollection(ResultSet rs, Class<T> valueClass, Collection<Map<String, T>> collection) throws SQLException {
        String[] names = getNames(rs);
        if (valueClass == Object.class) {
            while (rs.next()) {
                collection.add((Map<String, T>) toMap(rs, names));
            }
        } else {
            while (rs.next()) {
                collection.add(toMap(rs, valueClass, names));
            }
        }
        return collection;
    }

    private static <T> T buildInstance(ResultSet rs, Constructor<T> constructor, FieldSetHandler[] fields) throws Exception {
        T target = constructor.newInstance();
        int i = 1;
        for (FieldSetHandler field : fields) {
            if (field == null) continue;
            field.apply(target, rs, i++);
        }
        return target;
    }

    private static FieldSetHandler[] toFields(ResultSetMetaData metaData, Map<String, FieldSetHandler> fieldMap) throws Exception {
        int l = metaData.getColumnCount();
        FieldSetHandler[] fields = new FieldSetHandler[l];
        for (int i = 0; i < l; i++) {
            fields[i] = fieldMap.get(metaData.getColumnLabel(i + 1));
        }
        return fields;
    }
}

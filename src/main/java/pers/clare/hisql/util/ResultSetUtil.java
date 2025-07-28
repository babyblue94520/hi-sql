package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.function.ResultSetConvertHandler;
import pers.clare.hisql.store.SQLStore;
import pers.clare.hisql.store.SQLStoreColumn;
import pers.clare.hisql.support.ResultSetConverter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@UtilityClass
public class ResultSetUtil {


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

    public static <T> T toInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        if (rs.next()) {
            return buildInstance(sqlStore, rs);
        }
        return null;
    }

    public static <T> Set<T> toSetInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        SQLStoreColumn[] columns = getColumns(sqlStore, rs);
        Set<T> result = new HashSet<>();
        while (rs.next()) {
            result.add(buildInstance(sqlStore, rs, columns));
        }
        return result;
    }

    public static <T> List<T> toInstances(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        SQLStoreColumn[] columns = getColumns(sqlStore, rs);
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(buildInstance(sqlStore, rs, columns));
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

    public static <T> SQLStoreColumn[] getColumns(SQLStore<T> sqlStore, ResultSet rs) throws SQLException {
        Map<String, SQLStoreColumn> columnMap = sqlStore.getNameMapping();
        ResultSetMetaData metaData = rs.getMetaData();
        int l = metaData.getColumnCount();
        SQLStoreColumn[] columns = new SQLStoreColumn[l];
        for (int i = 0; i < l; ) {
            columns[i] = columnMap.get(metaData.getColumnLabel(++i));
        }
        return columns;
    }

    private static <T> T buildInstance(SQLStore<T> sqlStore, ResultSet rs) throws Exception {
        return buildInstance(sqlStore, rs, getColumns(sqlStore, rs));
    }

    private static <T> T buildInstance(SQLStore<T> sqlStore, ResultSet rs, SQLStoreColumn[] columns) throws Exception {
        T target = sqlStore.getConstructor().newInstance();
        int i = 0;
        for (SQLStoreColumn column : columns) {
            i++;
            if (column == null) continue;
            column.setValue(target, rs, i);
        }
        return target;
    }

    public static <T> T getValue(ResultSetConverter resultSetConverter, ResultSet rs, int index, Class<T> clazz) throws SQLException {
        return getValue(resultSetConverter.get(clazz), rs, index, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(ResultSetConvertHandler<T> converter, ResultSet rs, int index, Class<T> clazz) throws SQLException {
        if (converter == null) {
            if (clazz == Object.class) {
                return (T) rs.getObject(index);
            } else {
                return rs.getObject(index, clazz);
            }
        } else {
            return converter.apply(rs, index);
        }
    }
}

package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.function.ResultSetConvertHandler;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.store.SQLStoreColumn;
import pers.clare.hisql.support.field.FieldReflector;
import pers.clare.hisql.support.field.FieldResultSetter;
import pers.clare.hisql.support.field.FieldSetter;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class SQLStoreColumnUtil {
    private static final Map<Class<?>, SQLStoreColumn[]> cacheMap = new ConcurrentHashMap<>();

    public static SQLStoreColumn[] create(
            Class<?> clazz
            , SQLBasicService service
    ) {
        return cacheMap.computeIfAbsent(clazz, c -> scan(c, service));
    }

    private static SQLStoreColumn[] scan(
            Class<?> clazz
            , SQLBasicService service
    ) {
        List<SQLStoreColumn> result = new ArrayList<>();
        Map<String, Integer> indexMap = new HashMap<>();
        scan(clazz, service, result, indexMap);
        return result.toArray(new SQLStoreColumn[0]);
    }

    private static void scan(
            Class<?> clazz
            , SQLBasicService service
            , List<SQLStoreColumn> result
            , Map<String, Integer> indexMap
    ) {
        if (ClassUtil.isIgnore(clazz)) return;
        scan(clazz.getSuperclass(), service, result, indexMap);
        FieldReflector[] fieldReflectors = ClassUtil.getFieldReflectors(clazz);
        for (FieldReflector reflector : fieldReflectors) {
            SQLStoreColumn.SQLStoreColumnBuilder columnBuilder = SQLStoreColumn.builder();
            if (!parseAnnotations(service, columnBuilder, reflector)) continue;
            FieldSetter setter = reflector.getSetter();
            columnBuilder
                    .type(reflector.getWrapperType())
                    .setter(reflector.getSetter())
                    .getter(reflector.getGetter())
                    .resultSetter(createResultSetter(service, reflector, setter))
            ;
            SQLStoreColumn column = columnBuilder.build();
            Integer index = indexMap.get(column.getName());
            if (index == null) {
                indexMap.put(column.getName(), result.size());
                result.add(column);
            } else {
                result.set(index, column);
            }
        }
    }

    private static boolean parseAnnotations(
            SQLBasicService service
            , SQLStoreColumn.SQLStoreColumnBuilder columnBuilder
            , FieldReflector reflector
    ) {
        Field field = reflector.getField();
        String columnName = null;

        columnBuilder
                .id(false)
                .auto(false)
                .notNullable(false)
                .insertable(true)
                .updatable(true)
        ;
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof Transient) {
                return false;
            }
            if (annotation instanceof Id) {
                columnBuilder.id(true);
            } else if (annotation instanceof GeneratedValue) {
                columnBuilder.auto(true);
            } else if (annotation instanceof Column) {
                Column column = (Column) annotation;
                columnName = column.name();
                columnBuilder
                        .notNullable(!column.nullable())
                        .insertable(column.insertable())
                        .updatable(column.updatable())
                ;
            }

        }

        if (columnName == null || columnName.isEmpty()) {
            columnName = service.getNaming().turnCamelCase(field.getName());
        }
        columnBuilder.name(columnName);
        return true;
    }

    private FieldResultSetter createResultSetter(
            SQLBasicService service
            , FieldReflector fieldReflector
            , FieldSetter setter
    ) {
        Field field = fieldReflector.getField();
        Class<?> type = fieldReflector.getWrapperType();
        ResultSetConvertHandler<?> converter = service.getResultSetConverter().get(type);
        if (converter == null) {
            if (field.getType() == Object.class) {
                return (target, resultSet, index) ->
                        setter.apply(target, resultSet.getObject(index));
            } else {
                return (target, resultSet, index) ->
                        setter.apply(target, resultSet.getObject(index, type));
            }
        } else {
            return (target, resultSet, index) ->
                    setter.apply(target, converter.apply(resultSet, index));
        }
    }

}

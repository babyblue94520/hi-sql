package pers.clare.hisql.util;

import lombok.experimental.UtilityClass;
import pers.clare.hisql.service.SQLBasicService;
import pers.clare.hisql.store.SQLStoreColumn;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
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
        return cacheMap.computeIfAbsent(clazz, (c) -> scan(c, service));
    }

    private static SQLStoreColumn[] scan(
            Class<?> clazz
            , SQLBasicService service
    ) {
        List<SQLStoreColumn> result = new ArrayList<>();
        addFields(clazz, service, result);
        return result.toArray(new SQLStoreColumn[0]);
    }

    private static void addFields(
            Class<?> clazz
            , SQLBasicService service
            , List<SQLStoreColumn> result
    ) {
        if (SQLStoreUtil.isIgnore(clazz)) return;
        addFields(clazz.getSuperclass(), service, result);
        addFields(ClassUtil.getOrderFields(clazz), service, result);
    }

    private static void addFields(
            Collection<Field> fields
            , SQLBasicService service
            , List<SQLStoreColumn> result
    ) {
        for (Field field : fields) {
            SQLStoreColumn column = build(field, service);
            if (column == null) continue;
            result.add(column);
        }
    }

    public static SQLStoreColumn build(
            Field field
            , SQLBasicService service
    ) {
        int modifier = field.getModifiers();
        if (Modifier.isStatic(modifier)
            || Modifier.isFinal(modifier)
        ) return null;

        String columnName = null;
        boolean id = false;
        boolean auto = false;
        boolean nullable = true, insertable = true, updatable = true;
        Class<?> type = field.getType();
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation instanceof Transient) {
                return null;
            }
            if (annotation instanceof Id) {
                id = true;
            } else if (annotation instanceof GeneratedValue) {
                auto = true;
            } else if (annotation instanceof Column) {
                Column column = (Column) annotation;
                nullable = column.nullable();
                insertable = column.insertable();
                updatable = column.updatable();
                columnName = column.name();
            }
        }

        if (columnName == null || columnName.isEmpty()) {
            columnName = service.getNaming().turnCamelCase(field.getName());
        }

        return new SQLStoreColumn(
                columnName, field, id, auto, !nullable, insertable, updatable
                , service.getResultSetConverter().get(type)
        );
    }


}

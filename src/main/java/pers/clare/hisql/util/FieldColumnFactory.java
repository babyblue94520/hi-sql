package pers.clare.hisql.util;

import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.store.FieldColumn;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FieldColumnFactory {
    private static final Map<Class<?>, FieldColumn[]> classFieldsMap = new ConcurrentHashMap<>();

    public static FieldColumn[] get(NamingStrategy naming, Class<?> clazz) {
        return classFieldsMap.computeIfAbsent(clazz, (c) -> FieldColumnFactory.scan(naming, c));
    }

    private static FieldColumn[] scan(NamingStrategy naming, Class<?> clazz) {
        Map<String, Integer> orderMap = new HashMap<>();
        List<FieldColumn> result = new ArrayList<>();
        if (!isIgnore(clazz)) {
            putFields(naming, clazz, orderMap, result);
        }
        return result.toArray(new FieldColumn[0]);
    }

    private static void putFields(NamingStrategy naming, Class<?> clazz, Map<String, Integer> orderMap, List<FieldColumn> result) {
        if (isIgnore(clazz)) return;
        putFields(naming, clazz.getSuperclass(), orderMap, result);
        putFields(naming, ClassUtil.getOrderFields(clazz), orderMap, result);
    }

    private static void putFields(NamingStrategy naming, Collection<Field> fields, Map<String, Integer> orderMap, List<FieldColumn> result) {
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
                result.add(buildFieldColumn(naming, field));
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
                    result.set(index, buildFieldColumn(naming, field));
                }
            }
        }
    }

    public static FieldColumn buildFieldColumn(NamingStrategy naming, Field field) {
        Column column = field.getAnnotation(Column.class);
        String columnName = SQLStoreUtil.getColumnName(naming, field, column);
        boolean id = field.getAnnotation(Id.class) != null;
        boolean auto = field.getAnnotation(GeneratedValue.class) != null;
        boolean nullable = true, insertable = true, updatable = true;
        if (column != null) {
            nullable = column.nullable();
            insertable = column.insertable();
            updatable = column.updatable();
        }
        return new FieldColumn(field, id, auto, !nullable, insertable, updatable, columnName);
    }

    public static boolean isIgnore(Class<?> clazz) {
        return clazz == null
               || clazz.isPrimitive()
               || clazz.getName().startsWith("java")
               || clazz.isArray()
               || Collection.class.isAssignableFrom(clazz)
               || clazz.isEnum()
               || clazz.isInterface()
                ;
    }


}

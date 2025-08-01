package pers.clare.hisql.store;

import lombok.AccessLevel;
import lombok.Getter;
import pers.clare.hisql.function.ResultSetConvertHandler;
import pers.clare.hisql.util.ClassUtil;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
public class SQLStoreColumn {
    private final String name;
    private final Field field;
    private final boolean id;
    private final boolean auto;
    private final boolean notNullable;
    private final boolean insertable;
    private final boolean updatable;

    private final Class<?> type;
    @lombok.Getter(AccessLevel.NONE)
    private final ResultSetConvertHandler<?> converter;
    @lombok.Getter(AccessLevel.NONE)
    private final Setter setter;
    @lombok.Getter(AccessLevel.NONE)
    private final Getter getter;

    @SuppressWarnings("unchecked")
    public SQLStoreColumn(String name, Field field, boolean id, boolean auto, boolean notNullable, boolean insertable, boolean updatable, ResultSetConvertHandler<?> converter) {
        this.name = name;
        this.field = field;
        this.id = id;
        this.auto = auto;
        this.notNullable = notNullable;
        this.insertable = insertable;
        this.updatable = updatable;
        this.converter = converter;

        this.type = ClassUtil.toWrapperClass(field.getType());
        this.setter = createSetter();
        this.getter = createGetter();
    }

    public Object getValue(Object target) throws IllegalAccessException {
        return getter.apply(target);
    }

    public void setValue(Object target, ResultSet rs, int i) throws SQLException, IllegalAccessException {
        setter.apply(target, rs, i);
    }

    private Setter createSetter() {
        if (converter == null) {
            if (field.getType() == Object.class) {
                return this::setObjectValue;
            } else {
                return this::setTypeValue;
            }
        } else {
            return this::setConverterValue;
        }
    }

    private Getter createGetter() {
        return field::get;
    }

    public void setObjectValue(Object target, ResultSet resultSet, int index) throws SQLException, IllegalAccessException {
        field.set(target, resultSet.getObject(index));
    }

    public void setTypeValue(Object target, ResultSet resultSet, int index) throws SQLException, IllegalAccessException {
        field.set(target, resultSet.getObject(index, type));
    }

    public void setConverterValue(Object target, ResultSet resultSet, int index) throws SQLException, IllegalAccessException {
        field.set(target, converter.apply(resultSet, index));
    }

    @FunctionalInterface
    interface Setter {
        void apply(Object target, ResultSet resultSet, int index) throws SQLException, IllegalAccessException;
    }

    @FunctionalInterface
    interface Getter {
        Object apply(Object target) throws IllegalAccessException;
    }
}

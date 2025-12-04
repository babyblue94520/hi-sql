package pers.clare.hisql.store;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import pers.clare.hisql.support.field.FieldGetter;
import pers.clare.hisql.support.field.FieldResultSetter;
import pers.clare.hisql.support.field.FieldSetter;

import java.sql.ResultSet;
import java.sql.SQLException;

@Getter
@Builder
public class SQLStoreColumn {
    private final String name;
    private final boolean id;
    private final boolean auto;
    private final boolean notNullable;
    private final boolean insertable;
    private final boolean updatable;
    private final Class<?> type;

    @lombok.Getter(AccessLevel.NONE)
    private final FieldSetter setter;
    @lombok.Getter(AccessLevel.NONE)
    private final FieldResultSetter resultSetter;
    @lombok.Getter(AccessLevel.NONE)
    private final FieldGetter getter;

    public Object getValue(Object target) throws IllegalAccessException {
        return getter.apply(target);
    }

    public void setValue(Object target, ResultSet rs, int i) throws SQLException, IllegalAccessException {
        resultSetter.apply(target, rs, i);
    }

    public void setValue(Object target, Object value) throws IllegalAccessException {
        setter.apply(target, value);
    }
}

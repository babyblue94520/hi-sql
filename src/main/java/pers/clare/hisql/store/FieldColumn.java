package pers.clare.hisql.store;

import lombok.Getter;

import java.lang.reflect.Field;


@Getter
public class FieldColumn {
    private final Field field;
    private final boolean id;
    private final boolean auto;
    private final boolean notNullable;
    private final boolean insertable;
    private final boolean updatable;
    private final String columnName;

    public FieldColumn(Field field, boolean id, boolean auto, boolean notNullable, boolean insertable, boolean updatable, String columnName) {
        this.field = field;
        this.id = id;
        this.auto = auto;
        this.notNullable = notNullable;
        this.insertable = insertable;
        this.updatable = updatable;
        this.columnName = columnName;
    }

}

package pers.clare.hisql.store;

import java.lang.reflect.Field;


public class FieldColumn {
    private final Field field;
    private final boolean id;
    private final boolean auto;
    private final boolean nullable;
    private final boolean insertable;
    private final boolean updatable;
    private final String columnName;

    public FieldColumn(Field field, boolean id, boolean auto, boolean nullable, boolean insertable, boolean updatable, String columnName) {
        this.field = field;
        this.id = id;
        this.auto = auto;
        this.nullable = nullable;
        this.insertable = insertable;
        this.updatable = updatable;
        this.columnName = columnName;
    }

    public Field getField() {
        return field;
    }

    public boolean isId() {
        return id;
    }

    public boolean isAuto() {
        return auto;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public String getColumnName() {
        return columnName;
    }
}

package pers.clare.hisql.support.field;

@FunctionalInterface
public interface FieldSetter {
    void apply(Object target, Object value);
}
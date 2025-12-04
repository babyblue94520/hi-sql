package pers.clare.hisql.support.field;

@FunctionalInterface
public interface FieldGetter {
    Object apply(Object target);
}
package pers.clare.hisql.support;

@FunctionalInterface
public interface SqlReplaceCondition<T> {
    boolean match(T value);
}

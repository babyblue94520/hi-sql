package pers.clare.hisql.support;

import java.util.Collection;

public interface SqlReplace<T> {
    static SqlReplace<String> of(String value, String sql) {
        return new StringSqlReplace(value, sql);
    }

    static SqlReplace<String> of(String value, String sql, String emptySql) {
        return new StringSqlReplace(value, sql, emptySql);
    }

    static SqlReplace<Collection<?>> of(Collection<?> value, String sql) {
        return new CollectionSqlReplace(value, sql);
    }

    static SqlReplace<Collection<?>> of(Collection<?> value, String sql, String emptySql) {
        return new CollectionSqlReplace(value, sql, emptySql);
    }

    static SqlReplace<Object> of(Object value, String sql) {
        return new ObjectSqlReplace(value, sql);
    }

    static SqlReplace<Object> of(Object value, String sql, String emptySql) {
        return new ObjectSqlReplace(value, sql, emptySql);
    }

    T getValue();

    String getSql();


}

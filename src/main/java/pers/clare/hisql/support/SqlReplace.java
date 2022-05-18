package pers.clare.hisql.support;

public interface SqlReplace {
    Object getValue();

    String getSql();

    static StringSqlReplace of(String value, String sql) {
        return new StringSqlReplace(value, sql);
    }

    static StringSqlReplace of(String value, String sql, String emptySql) {
        return new StringSqlReplace(value, sql, emptySql);
    }

    static ObjectSqlReplace of(Object value, String sql) {
        return new ObjectSqlReplace(value, sql);
    }

    static ObjectSqlReplace of(Object value, String sql, String emptySql) {
        return new ObjectSqlReplace(value, sql, emptySql);
    }
}

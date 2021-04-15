package pers.clare.hisql.function;


import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetHandler<T, R> {
    R apply(ResultSet resultSet, Class<T> valueType) throws Exception;
}

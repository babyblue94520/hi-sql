package pers.clare.hisql.function;


import pers.clare.hisql.support.ResultSetConverter;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetHandler<T, R> {
    R apply(ResultSetConverter resultSetConverter, ResultSet resultSet, Class<T> returnType) throws Exception;
}

package pers.clare.hisql.function;

import pers.clare.hisql.store.SQLStore;

import java.sql.ResultSet;

@FunctionalInterface
public interface StoreResultSetHandler<T, R> {
    R apply(ResultSet resultSet, SQLStore<T> sqlStore) throws Exception;
}

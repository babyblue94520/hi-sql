package pers.clare.hisql.repository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.store.SQLStore;

@SuppressWarnings("unused")
@Repository
public interface SQLRepository {
    <R> R connection(String sql, Object[] args, ConnectionCallback<R> callback);

    <R> R prepared(String sql, PreparedStatementCallback<R> callback);

    <R> R resultSet(String sql, Object[] args, ResultSetCallback<R> callback);


    <T> T executeInsert(Class<T> keyType, String sql, Object... args);

    @NonNull
    int executeUpdate(String sql, Object... args);

    <T> SQLStore<T> buildSQLStore(Class<T> clazz);
}

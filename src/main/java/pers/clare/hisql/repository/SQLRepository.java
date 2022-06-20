package pers.clare.hisql.repository;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;

@SuppressWarnings("unused")
@Repository
public interface SQLRepository {
    <R> R connection(String sql, Object[] args, ConnectionCallback<R> callback);

    <R> R prepared(String sql, PreparedStatementCallback<R> callback);

    <R> R query(String sql, Object[] args, ResultSetCallback<R> callback);

    @NonNull
    int insert(String sql, Object... args);

    <T> T insert(Class<T> keyType, String sql, Object... args);

    @NonNull
    int update(String sql, Object... args);
}

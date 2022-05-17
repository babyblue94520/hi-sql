package pers.clare.hisql.repository;

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

    int insert(String sql, Object... args);

    /**
     * @param keyType auto key class
     * @return auto key
     */
    <T> T insert(Class<T> keyType, String sql, Object... args);

    int update(String sql, Object... args);
}

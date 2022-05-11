package pers.clare.hisql.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;

@Repository
public interface SQLRepository {
    <R> R connection(String sql, Object[] args, ConnectionCallback<R> callback);

    <R> R connection(boolean readonly, String sql, Object[] args, ConnectionCallback<R> callback);

    <R> R prepared(String sql, Object[] args, PreparedStatementCallback<R> callback);

    <R> R prepared(boolean readonly, String sql, Object[] args, PreparedStatementCallback<R> callback);

    <R> R query(String sql, Object[] args, ResultSetCallback<R> callback);

    <R> R query(boolean readonly, String sql, Object[] args, ResultSetCallback<R> callback);

    <R> R insert(String sql, Object[] args, ResultSetCallback<R> callback);

    <R> R update(String sql, Object[] args, ResultSetCallback<R> callback);
}

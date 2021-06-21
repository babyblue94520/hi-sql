package pers.clare.hisql.repository;

import org.springframework.stereotype.Repository;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;

@Repository
public interface SQLRepository {

    <R> R query(String sql, ResultSetCallback<R> callback, Object... parameters);

    <R> R connection(ConnectionCallback<R> callback);

    <R> R preparedStatement(String sql, PreparedStatementCallback<R> callback);

    <R> R query(boolean readonly,String sql, ResultSetCallback<R> callback, Object... parameters);

    <R> R connection(boolean readonly, ConnectionCallback<R> callback);

    <R> R preparedStatement(boolean readonly, String sql, PreparedStatementCallback<R> callback);
}

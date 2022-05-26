package pers.clare.hisql.data.repository;

import pers.clare.hisql.annotation.HiSql;
import pers.clare.hisql.function.ConnectionCallback;
import pers.clare.hisql.function.PreparedStatementCallback;
import pers.clare.hisql.function.ResultSetCallback;
import pers.clare.hisql.repository.SQLRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallbackRepository extends SQLRepository {
    @HiSql("select :value")
    <T> T connection(T value, ConnectionCallback<T> callback);

    @HiSql("select ?")
    <T> T connectionPrepared(T value, ConnectionCallback<T> callback);

    @HiSql("select :value")
    <T> T prepared(T value, PreparedStatementCallback<T> callback);

    @HiSql("select ?")
    <T> T prepared2(PreparedStatementCallback<T> callback);

    @HiSql("select :value")
    <T> T resultSet(T value, ResultSetCallback<T> callback);
}

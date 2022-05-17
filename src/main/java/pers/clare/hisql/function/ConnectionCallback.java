package pers.clare.hisql.function;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("RedundantThrows")
@FunctionalInterface
public interface ConnectionCallback<R> {
    R apply(Connection connection, String sql, Object[] parameters) throws SQLException;
}

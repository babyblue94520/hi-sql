package pers.clare.hisql.function;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("RedundantThrows")
@FunctionalInterface
public interface ConnectionOnlyCallback<R> {
    R apply(Connection connection) throws SQLException, IllegalAccessException;
}

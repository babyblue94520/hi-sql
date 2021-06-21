package pers.clare.hisql.function;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallback<R> {
    R apply(Connection connection) throws SQLException;
}

package pers.clare.hisql.function;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("RedundantThrows")
@FunctionalInterface
public interface ConnectionKeyTypeCallback<T, R> {
    R apply(Connection connection, Class<T> keyType, String sql, Object[] parameters) throws SQLException;
}

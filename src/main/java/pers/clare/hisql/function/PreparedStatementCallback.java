package pers.clare.hisql.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface PreparedStatementCallback<R> {
    R apply(PreparedStatement preparedStatement, Object[] parameters) throws SQLException;
}

package pers.clare.hisql.function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@SuppressWarnings("RedundantThrows")
@FunctionalInterface
public interface PreparedStatementCallback<R> {
    R apply(PreparedStatement preparedStatement) throws SQLException;
}

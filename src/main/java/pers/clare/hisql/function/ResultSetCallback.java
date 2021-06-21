package pers.clare.hisql.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetCallback<R> {
    R apply(ResultSet resultSet) throws SQLException;
}

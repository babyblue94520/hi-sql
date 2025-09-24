package pers.clare.hisql.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetConvertHandler<T> {
    T apply(ResultSet resultSet, int index) throws SQLException;
}

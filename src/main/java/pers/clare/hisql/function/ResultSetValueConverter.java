package pers.clare.hisql.function;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetValueConverter<T> {
    T apply(ResultSet resultSet, int index) throws SQLException;
}

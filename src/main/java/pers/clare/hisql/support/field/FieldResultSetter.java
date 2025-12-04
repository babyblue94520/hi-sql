package pers.clare.hisql.support.field;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface FieldResultSetter {
    void apply(Object target, ResultSet resultSet, int index) throws SQLException, IllegalAccessException;
}
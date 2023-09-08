package pers.clare.hisql.function;


import java.sql.ResultSet;

@FunctionalInterface
public interface FieldSetter {
    void apply(Object target, ResultSet resultSet, int index) throws Exception;
}

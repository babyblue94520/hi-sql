package pers.clare.hisql.function;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionCallback<R> {
    R apply(Connection connection);
}

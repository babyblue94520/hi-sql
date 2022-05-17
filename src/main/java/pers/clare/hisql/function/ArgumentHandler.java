package pers.clare.hisql.function;

import pers.clare.hisql.exception.HiSqlException;

@FunctionalInterface
public interface ArgumentHandler<T> {
    T apply(Object[] arguments) throws HiSqlException;
}

package pers.clare.hisql.function;

import pers.clare.hisql.exception.HiSqlException;

@FunctionalInterface
public interface ArgumentGetHandler {
    Object apply(Object[] arguments) throws HiSqlException;


}

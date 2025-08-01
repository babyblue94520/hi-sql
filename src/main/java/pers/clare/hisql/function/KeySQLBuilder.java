package pers.clare.hisql.function;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.query.SQLQueryBuilder;

@FunctionalInterface
public interface KeySQLBuilder<KEY> {
    String apply(SQLQueryBuilder sqlQueryBuilder, KEY key) throws HiSqlException;
}

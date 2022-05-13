package pers.clare.hisql.function;

import pers.clare.hisql.exception.HiSqlException;
import pers.clare.hisql.query.SQLQueryBuilder;

@FunctionalInterface
public interface KeySQLBuilder<Key> {
    String apply(SQLQueryBuilder sqlQueryBuilder, Key key) throws HiSqlException;
}

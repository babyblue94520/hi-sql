package pers.clare.hisql.data;

import pers.clare.hisql.annotation.EnableHiSql;

@EnableHiSql(
        resultSetConverter = CustomResultSetConverter.class
)
public class HiSqlConfig {
}

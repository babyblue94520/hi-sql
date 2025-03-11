package pers.clare.hisql.data;

import pers.clare.hisql.annotation.EnableHiSql;
import pers.clare.hisql.page.H2PaginationMode;

@EnableHiSql(
        resultSetConverter = CustomResultSetConverter.class
        , beanNamePrefix = "test"
        , paginationMode = H2PaginationMode.class
)
public class HiSqlConfig {
}

package pers.clare.hisql.service.impl;

import lombok.Getter;
import lombok.Setter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.support.CommandTypeParser;
import pers.clare.hisql.support.ResultSetConverter;

import javax.sql.DataSource;

@Setter
@Getter
public class SQLServiceImpl implements SQLService {
    private DataSource dataSource;

    private String xmlRoot;

    private PaginationMode paginationMode;

    private NamingStrategy naming;

    private ResultSetConverter resultSetConverter;

    private CommandTypeParser commandTypeParser;

}

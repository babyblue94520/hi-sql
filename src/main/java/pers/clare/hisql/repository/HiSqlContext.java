package pers.clare.hisql.repository;

import lombok.Getter;
import lombok.Setter;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.support.ResultSetConverter;

@Setter
@Getter
@SuppressWarnings("unused")
public class HiSqlContext {

    private String xmlRoot;

    private PaginationMode paginationMode;

    private NamingStrategy naming;

    private ResultSetConverter resultSetConverter;

}

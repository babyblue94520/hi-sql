package pers.clare.hisql.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import pers.clare.hisql.naming.LowerCaseNamingStrategy;
import pers.clare.hisql.page.MySQLPaginationMode;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.support.ResultSetConverter;

import javax.sql.DataSource;

@Configuration
public class SqlStoreConfig {

    @Bean
    @Primary
    public SQLStoreService sqlStoreService(DataSource dataSource) {
        SQLStoreService service = new SQLStoreService();
        service.setDataSource(dataSource);
        service.setPaginationMode(new MySQLPaginationMode());
        service.setNaming(new LowerCaseNamingStrategy());
        service.setResultSetConverter(new ResultSetConverter());
        return service;
    }
}

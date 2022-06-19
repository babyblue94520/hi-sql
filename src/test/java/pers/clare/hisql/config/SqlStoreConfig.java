package pers.clare.hisql.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pers.clare.hisql.naming.LowerCaseNamingStrategy;
import pers.clare.hisql.page.MySQLPaginationMode;
import pers.clare.hisql.repository.HiSqlContext;
import pers.clare.hisql.service.SQLStoreService;

import javax.sql.DataSource;

@Configuration
public class SqlStoreConfig {

    @Bean
    public SQLStoreService sqlStoreService(DataSource dataSource) {
        HiSqlContext hiSqlContext = new HiSqlContext();
        hiSqlContext.setPaginationMode(new MySQLPaginationMode());
        hiSqlContext.setNaming(new LowerCaseNamingStrategy());
        return new SQLStoreService(hiSqlContext, dataSource);

    }
}

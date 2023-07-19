package pers.clare.hisql.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import pers.clare.hisql.naming.LowerCaseNamingStrategy;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.MySQLPaginationMode;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.repository.SQLRepositoryFactoryBean;
import pers.clare.hisql.repository.SQLScanRegistrar;
import pers.clare.hisql.support.CommandTypeParser;
import pers.clare.hisql.support.ResultSetConverter;

import java.lang.annotation.*;

@SuppressWarnings("unused")
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SQLScanRegistrar.class})
@Configuration
public @interface EnableHiSql {
    @AliasFor(
            annotation = Configuration.class
    )
    String value() default "";

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    /**
     * DataSource bean name.
     */
    String dataSourceRef() default "";

    /**
     * Xml files root path.
     * <p>
     * resources/hisql
     */
    String xmlRootPath() default "hisql";

    /**
     * SQLStoreService bean name prefix. Default package#SQLStoreService
     */
    String beanNamePrefix() default "";

    Class<? extends PaginationMode> paginationMode() default MySQLPaginationMode.class;

    Class<? extends NamingStrategy> naming() default LowerCaseNamingStrategy.class;

    Class<? extends ResultSetConverter> resultSetConverter() default ResultSetConverter.class;

    Class<? extends CommandTypeParser> commandTypeParser() default CommandTypeParser.class;

    Class<? extends SQLRepositoryFactoryBean> factoryBean() default SQLRepositoryFactoryBean.class;

}

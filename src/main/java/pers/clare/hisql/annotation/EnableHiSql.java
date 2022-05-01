package pers.clare.hisql.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import pers.clare.hisql.naming.LowerCaseNamingStrategy;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.MySQLPaginationMode;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.repository.SQLRepositoryFactoryBean;
import pers.clare.hisql.repository.SQLScanRegistrar;

import java.lang.annotation.*;

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
    String dataSourceRef() default "";
    String readDataSourceRef() default "";
    String contextRef() default "";
    String xmlRootPath() default "hisql"; // resources/hisql
    Class<? extends PaginationMode> paginationMode() default MySQLPaginationMode.class;
    Class<? extends NamingStrategy> naming() default LowerCaseNamingStrategy.class;
    Class<? extends SQLRepositoryFactoryBean> factoryBean() default SQLRepositoryFactoryBean.class;
}

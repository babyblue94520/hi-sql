package pers.clare.hisql.annotation;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import pers.clare.hisql.repository.SQLScanRegistrar;
import pers.clare.hisql.repository.SQLRepositoryFactoryBean;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SQLScanRegistrar.class})
@Component
public @interface EnableHiSql {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
    String[] basePackages() default {};
    Class<?>[] basePackageClasses() default {};
    String dataSourceRef() default "";
    String readDataSourceRef() default "";
    String contextRef() default "";
    Class<? extends SQLRepositoryFactoryBean> factoryBean() default SQLRepositoryFactoryBean.class;
}

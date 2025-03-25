package pers.clare.hisql.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HiSql {
    // native sql string
    String value() default "";

    // Find sql from XML by name
    String name() default "";

    boolean returnIncrementKey() default false;
}

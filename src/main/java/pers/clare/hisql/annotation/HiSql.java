package pers.clare.hisql.annotation;


import pers.clare.hisql.constant.CommandType;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HiSql {
    // native sql string
    String value() default "";

    // Find sql from XML by name
    String name() default "";

    /**
     * 0: Automatic judgment.
     *
     * @see CommandType
     */
    int commandType() default 0;

    boolean returnIncrementKey() default false;
}

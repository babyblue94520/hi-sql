package pers.clare.hisql.aop;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlConnectionReuse {
    boolean transaction() default false;
    int isolation() default Connection.TRANSACTION_NONE;
    boolean readonly() default false;
}

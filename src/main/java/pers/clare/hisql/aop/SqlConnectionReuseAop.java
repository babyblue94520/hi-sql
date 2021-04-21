package pers.clare.hisql.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pers.clare.hisql.support.ConnectionReuseHolder;
import pers.clare.hisql.support.ConnectionReuseManager;

@Aspect
@Order(Integer.MAX_VALUE)
@Component
public class SqlConnectionReuseAop {

    @Around("@annotation(pers.clare.hisql.aop.SqlConnectionReuse)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        SqlConnectionReuse sqlConnectionReuse = ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaredAnnotation(SqlConnectionReuse.class);
        ConnectionReuseManager manager = ConnectionReuseHolder.init(
                sqlConnectionReuse.transaction()
                , sqlConnectionReuse.isolation()
                , sqlConnectionReuse.readonly()
        );
        try {
            Object result = joinPoint.proceed();
            manager.commit();
            return result;
        } catch (Exception e) {
            manager.rollback();
            throw e;
        } finally {
            manager.close();
        }
    }
}

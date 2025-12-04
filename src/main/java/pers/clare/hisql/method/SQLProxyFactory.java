package pers.clare.hisql.method;

import lombok.experimental.UtilityClass;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLCrudRepositoryImpl;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.repository.SQLRepositoryImpl;
import pers.clare.hisql.service.SQLService;

@UtilityClass
public class SQLProxyFactory {

    public static ProxyFactory build(
            Class<?> clazz
            , SQLService service
    ) {

        if (!SQLRepository.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(String.format("%s must inherit %s interface", clazz, SQLRepository.class.getSimpleName()));
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        Object target;
        if (SQLCrudRepository.class.isAssignableFrom(clazz)) {
            target = new SQLCrudRepositoryImpl<>(service, clazz);
        } else {
            target = new SQLRepositoryImpl<>(service);
        }
        proxyFactory.setInterfaces(clazz);
        proxyFactory.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        proxyFactory.addAdvice(new SQLMethodInterceptor(clazz, service, target));
        return proxyFactory;
    }
}

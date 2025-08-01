package pers.clare.hisql.method;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLCrudRepositoryImpl;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.repository.SQLRepositoryImpl;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.service.impl.SQLServiceImpl;

public class SQLProxyFactory {

    public static ProxyFactory build(
            Class<?> clazz
            , SQLServiceImpl service
    ) {

        if (!SQLRepository.class.isAssignableFrom(clazz)) {
            throw new Error(String.format("%s must inherit %s interface", clazz, SQLRepository.class.getSimpleName()));
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        Object target;
        if (SQLCrudRepository.class.isAssignableFrom(clazz)) {
            target = new SQLCrudRepositoryImpl<>(service, clazz);
            proxyFactory.setInterfaces(clazz, SQLCrudRepository.class);
        } else {
            target = new SQLRepositoryImpl<SQLService>(service);
            proxyFactory.setInterfaces(clazz, SQLRepository.class);
        }
        proxyFactory.setTarget(target);
        proxyFactory.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        proxyFactory.addAdvice(new SQLMethodInterceptor(clazz, target, SQLMethodFactory.create(clazz, service)));
        return proxyFactory;
    }
}

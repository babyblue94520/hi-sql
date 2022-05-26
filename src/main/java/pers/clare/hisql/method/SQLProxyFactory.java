package pers.clare.hisql.method;

import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLCrudRepositoryImpl;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.repository.SQLRepositoryImpl;
import pers.clare.hisql.service.SQLService;
import pers.clare.hisql.service.SQLStoreService;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;

public class SQLProxyFactory {

    public static ProxyFactory build(
            Class<?> clazz
            , SQLStoreService sqlStoreService
    ) {

        if (!SQLRepository.class.isAssignableFrom(clazz)) {
            throw new Error(String.format("%s must inherit %s interface", clazz, SQLRepository.class.getSimpleName()));
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        Object target;
        if (SQLCrudRepository.class.isAssignableFrom(clazz)) {
            target = new SQLCrudRepositoryImpl<>(sqlStoreService, clazz);
            proxyFactory.setInterfaces(clazz, SQLCrudRepository.class);
        } else {
            target = new SQLRepositoryImpl<SQLService>(sqlStoreService);
            proxyFactory.setInterfaces(clazz, SQLRepository.class);
        }
        proxyFactory.setTarget(target);
        proxyFactory.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        proxyFactory.addAdvice(new SQLMethodInterceptor(clazz, target, SQLMethodFactory.create(clazz, sqlStoreService)));
        return proxyFactory;
    }
}

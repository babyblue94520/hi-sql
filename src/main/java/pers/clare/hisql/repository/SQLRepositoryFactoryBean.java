package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.util.Assert;
import pers.clare.hisql.method.SQLMethodFactory;
import pers.clare.hisql.method.SQLMethodInterceptor;
import pers.clare.hisql.repository.SQLCrudRepository;
import pers.clare.hisql.repository.SQLCrudRepositoryImpl;
import pers.clare.hisql.repository.SQLRepository;
import pers.clare.hisql.repository.SQLRepositoryImpl;
import pers.clare.hisql.service.SQLStoreService;



public class SQLRepositoryFactoryBean implements InitializingBean, FactoryBean<Object>, BeanClassLoaderAware,
        BeanFactoryAware {
    private static final Logger log = LogManager.getLogger();

    protected ClassLoader classLoader;
    protected BeanFactory beanFactory;

    private final Class<?> repositoryInterface;

    private final SQLStoreService sqlStoreService;

    private Object repository;

    public SQLRepositoryFactoryBean(
            Class<?> repositoryInterface
            , SQLStoreService sqlStoreService
    ) {
        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        Assert.notNull(sqlStoreService, "Repository sqlStoreService must not be null!");
        this.repositoryInterface = repositoryInterface;
        this.sqlStoreService = sqlStoreService;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getObject() {
        return this.repository;
    }

    @Override
    public Class<?> getObjectType() {
        return this.repositoryInterface;
    }

    @Override
    public void afterPropertiesSet() {
        this.repository = getRepository(sqlStoreService);
    }

    public Object getRepository(
            SQLStoreService sqlStoreService
    ) {
        if (!SQLRepository.class.isAssignableFrom(repositoryInterface)) {
            throw new Error(String.format("%s must inherit %s interface", repositoryInterface, SQLRepository.class.getSimpleName()));
        }
        ProxyFactory result = new ProxyFactory();
        Object target;
        if (SQLCrudRepository.class.isAssignableFrom(repositoryInterface)) {
            target = new SQLCrudRepositoryImpl<>(sqlStoreService, repositoryInterface);
            result.setInterfaces(repositoryInterface, SQLCrudRepository.class);
        } else {
            target = new SQLRepositoryImpl(sqlStoreService);
            result.setInterfaces(repositoryInterface, SQLRepository.class);
        }
        result.setTarget(target);
        result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        result.addAdvice(new SQLMethodInterceptor(repositoryInterface, SQLMethodFactory.create(sqlStoreService, repositoryInterface), target));
        Object repository = result.getProxy(classLoader);

        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }
}

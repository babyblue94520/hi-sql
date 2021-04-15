package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.method.SQLMethodFactory;
import pers.clare.hisql.method.SQLMethodInterceptor;
import pers.clare.hisql.service.SQLStoreService;

public class SQLRepositoryFactory implements BeanClassLoaderAware, BeanFactoryAware {
    private static final Logger log = LogManager.getLogger();

    protected HiSqlContext context;
    protected ClassLoader classLoader;
    protected BeanFactory beanFactory;

    public void setContext(HiSqlContext context) {
        this.context = context;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public <T> T getRepository(
            Class<T> repositoryInterface
            , SQLStoreService sqlStoreService
    ) {
        if (!SQLRepository.class.isAssignableFrom(repositoryInterface)) {
            throw new Error(String.format("%s must inherit %s interface", repositoryInterface, SQLRepository.class.getSimpleName()));
        }
        ProxyFactory result = new ProxyFactory();
        Object target;
        if (SQLCrudRepository.class.isAssignableFrom(repositoryInterface)) {
            target = new SQLCrudRepositoryImpl(context, sqlStoreService, repositoryInterface);
            result.setInterfaces(repositoryInterface, SQLCrudRepository.class);
        } else {
            target = new SQLRepository() {
            };
            result.setInterfaces(repositoryInterface, SQLRepository.class);
        }
        result.setTarget(target);
        result.addAdvisor(ExposeInvocationInterceptor.ADVISOR);
        result.addAdvice(new SQLMethodInterceptor(repositoryInterface, SQLMethodFactory.create(context, sqlStoreService, repositoryInterface), target));
        T repository = (T) result.getProxy(classLoader);

        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }
}

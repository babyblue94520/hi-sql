package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.util.Assert;
import pers.clare.hisql.method.SQLProxyFactory;
import pers.clare.hisql.service.SQLStoreService;

public class SQLRepositoryFactoryBean implements InitializingBean, FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {
    private static final Logger log = LogManager.getLogger();
    private final Class<?> repositoryInterface;
    private final String serviceBeanName;
    protected ClassLoader classLoader;
    private Object repository;
    private BeanFactory beanFactory;

    public SQLRepositoryFactoryBean(
            Class<?> repositoryInterface
            , String serviceBeanName
    ) {
        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        this.repositoryInterface = repositoryInterface;
        this.serviceBeanName = serviceBeanName;
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
        this.repository = getRepository();
    }

    public Object getRepository() {
        Object repository = SQLProxyFactory.build(repositoryInterface, beanFactory.getBean(serviceBeanName, SQLStoreService.class)).getProxy(classLoader);
        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }
}

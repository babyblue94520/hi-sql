package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class SQLRepositoryFactoryBean implements InitializingBean, FactoryBean<Object>, BeanClassLoaderAware {
    private static final Logger log = LogManager.getLogger();
    private final Class<?> repositoryInterface;
    private final ProxyFactory proxyFactory;
    protected ClassLoader classLoader;
    private Object repository;

    public SQLRepositoryFactoryBean(
            Class<?> repositoryInterface
            , ProxyFactory proxyFactory
    ) {
        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        this.repositoryInterface = repositoryInterface;
        this.proxyFactory = proxyFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
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
        Object repository = proxyFactory.getProxy(classLoader);
        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }
}

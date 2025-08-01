package pers.clare.hisql.repository;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.util.Assert;
import pers.clare.hisql.method.SQLProxyFactory;
import pers.clare.hisql.service.impl.SQLServiceImpl;

@Log4j2
public class SQLRepositoryFactoryBean implements InitializingBean, FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {
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
        Object repository = SQLProxyFactory.build(repositoryInterface, beanFactory.getBean(serviceBeanName, SQLServiceImpl.class)).getProxy(classLoader);
        if (log.isDebugEnabled()) {
            log.debug("Finished creation of repository instance for {}.", repositoryInterface.getName());
        }
        return repository;
    }
}

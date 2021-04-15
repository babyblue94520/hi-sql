package pers.clare.hisql.repository;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.Assert;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.naming.DefaultNamingStrategy;
import pers.clare.hisql.page.MySQLPageMode;
import pers.clare.hisql.service.SQLStoreService;

import javax.sql.DataSource;

public class SQLRepositoryFactoryBean<T> implements InitializingBean, FactoryBean<T>, BeanClassLoaderAware,
        BeanFactoryAware, ApplicationEventPublisherAware {
    protected ClassLoader classLoader;
    protected BeanFactory beanFactory;

    private final Class<? extends T> repositoryInterface;

    private SQLRepositoryFactory factory;

    private AnnotationAttributes annotationAttributes;

    private T repository;

    public SQLRepositoryFactoryBean(
            Class<? extends T> repositoryInterface
            , AnnotationAttributes annotationAttributes
    ) {
        Assert.notNull(repositoryInterface, "Repository interface must not be null!");
        Assert.notNull(annotationAttributes, "Repository annotationAttributes must not be null!");
        this.repositoryInterface = repositoryInterface;
        this.annotationAttributes = annotationAttributes;
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
    public T getObject() throws Exception {
        return this.repository;
    }

    @Override
    public Class<?> getObjectType() {
        return this.repositoryInterface;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String dataSourceName = this.annotationAttributes.getString("dataSourceRef");
        String readDataSourceName = this.annotationAttributes.getString("readDataSourceRef");
        String contextName = this.annotationAttributes.getString("contextRef");

        DataSource write;
        if (dataSourceName == null || dataSourceName.length() == 0) {
            write = beanFactory.getBean(DataSource.class);
        } else {
            write = (DataSource) beanFactory.getBean(dataSourceName);
        }
        DataSource read = write;
        if (readDataSourceName != null && readDataSourceName.length() > 0)
            read = (DataSource) beanFactory.getBean(readDataSourceName);
        HiSqlContext hiSqlContext;
        if (contextName == null || contextName.length() == 0) {
            hiSqlContext = new HiSqlContext();
            hiSqlContext.setNaming(new DefaultNamingStrategy());
            hiSqlContext.setPageMode(new MySQLPageMode());
        } else {
            hiSqlContext = (HiSqlContext) beanFactory.getBean(contextName);
        }
        SQLStoreService sqlStoreService = new SQLStoreService(hiSqlContext, write, read);
        this.factory = new SQLRepositoryFactory();
        this.factory.setContext(hiSqlContext);
        this.factory.setBeanClassLoader(classLoader);
        this.factory.setBeanFactory(beanFactory);
        this.repository = this.factory.getRepository(repositoryInterface, sqlStoreService);
    }

}

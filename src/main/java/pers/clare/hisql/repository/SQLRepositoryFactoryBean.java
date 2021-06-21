package pers.clare.hisql.repository;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.Assert;
import pers.clare.hisql.HiSqlContext;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLStoreService;

import javax.sql.DataSource;

public class SQLRepositoryFactoryBean<T> implements InitializingBean, FactoryBean<T>, BeanClassLoaderAware,
        BeanFactoryAware, ApplicationEventPublisherAware {
    protected ClassLoader classLoader;
    protected BeanFactory beanFactory;

    private final Class<? extends T> repositoryInterface;

    private final AnnotationAttributes annotationAttributes;

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
    public T getObject() {
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
        String dataSourceName = annotationAttributes.getString("dataSourceRef");
        String readDataSourceName = annotationAttributes.getString("readDataSourceRef");
        String contextName = annotationAttributes.getString("contextRef");
        String xmlRootPath = annotationAttributes.getString("xmlRootPath");
        Class<? extends NamingStrategy> namingClass = annotationAttributes.getClass("naming");
        Class<? extends PaginationMode> paginationModeClass = annotationAttributes.getClass("paginationMode");

        DataSource write;
        if (dataSourceName.length() == 0) {
            write = beanFactory.getBean(DataSource.class);
        } else {
            write = (DataSource) beanFactory.getBean(dataSourceName);
        }
        DataSource read = write;
        if (readDataSourceName.length() > 0)
            read = (DataSource) beanFactory.getBean(readDataSourceName);
        HiSqlContext hiSqlContext;
        if (contextName.length() == 0) {
            hiSqlContext = new HiSqlContext();
        } else {
            hiSqlContext = (HiSqlContext) beanFactory.getBean(contextName);
        }
        if (hiSqlContext.getXmlRoot() == null) {
            hiSqlContext.setXmlRoot(xmlRootPath);
        }
        if (hiSqlContext.getPaginationMode() == null) {
            hiSqlContext.setPaginationMode(paginationModeClass.newInstance());
        }
        if (hiSqlContext.getNaming() == null) {
            hiSqlContext.setNaming(namingClass.newInstance());
        }

        SQLStoreService sqlStoreService = new SQLStoreService(hiSqlContext, write, read);
        SQLRepositoryFactory factory = new SQLRepositoryFactory();
        factory.setContext(hiSqlContext);
        factory.setBeanClassLoader(classLoader);
        factory.setBeanFactory(beanFactory);
        this.repository = factory.getRepository(repositoryInterface, sqlStoreService);
    }

}

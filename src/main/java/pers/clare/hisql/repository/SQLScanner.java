package pers.clare.hisql.repository;

import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLStoreService;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.Assert.notNull;

@SuppressWarnings("unused")
public class SQLScanner implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware
        , BeanNameAware, BeanFactoryAware, BeanClassLoaderAware {

    protected BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private ClassLoader classLoader;

    private String beanName;
    private String basePackage;
    private boolean processPropertyPlaceHolders;
    private AnnotationAttributes annotationAttributes;
    private SQLStoreService sqlStoreService;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private Environment getEnvironment() {
        return this.applicationContext.getEnvironment();
    }

    @Override
    public void afterPropertiesSet() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        notNull(this.basePackage, "Property 'basePackage' is required");
        String dataSourceName = annotationAttributes.getString("dataSourceRef");
        String contextName = annotationAttributes.getString("contextRef");
        String xmlRootPath = annotationAttributes.getString("xmlRootPath");
        Class<? extends NamingStrategy> namingClass = annotationAttributes.getClass("naming");
        Class<? extends PaginationMode> paginationModeClass = annotationAttributes.getClass("paginationMode");

        DataSource dataSource;
        if (dataSourceName.length() == 0) {
            dataSource = beanFactory.getBean(DataSource.class);
        } else {
            dataSource = (DataSource) beanFactory.getBean(dataSourceName);
        }

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
            hiSqlContext.setPaginationMode(paginationModeClass.getConstructor().newInstance());
        }
        if (hiSqlContext.getNaming() == null) {
            hiSqlContext.setNaming(namingClass.getConstructor().newInstance());
        }
        this.sqlStoreService = new SQLStoreService(hiSqlContext, dataSource);
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        if (this.processPropertyPlaceHolders) {
            processPropertyPlaceHolders();
        }
        SQLRepositoryScanner scanner = new SQLRepositoryScanner(beanDefinitionRegistry, classLoader, annotationAttributes, sqlStoreService);
        scanner.setResourceLoader(this.applicationContext);
        scanner.registerFilters();
        scanner.scan(
                StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    private void processPropertyPlaceHolders() {
        Map<String, PropertyResourceConfigurer> propertyResourceConfigurer = applicationContext.getBeansOfType(PropertyResourceConfigurer.class,
                false, false);

        if (!propertyResourceConfigurer.isEmpty() && applicationContext instanceof ConfigurableApplicationContext) {
            BeanDefinition mapperScannerBean = ((ConfigurableApplicationContext) applicationContext).getBeanFactory()
                    .getBeanDefinition(beanName);

            // PropertyResourceConfigurer does not expose any methods to explicitly perform
            // property placeholder substitution. Instead, create a BeanFactory that just
            // contains this mapper scanner and post process the factory.
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            factory.registerBeanDefinition(beanName, mapperScannerBean);

            for (PropertyResourceConfigurer prc : propertyResourceConfigurer.values()) {
                prc.postProcessBeanFactory(factory);
            }

            PropertyValues values = mapperScannerBean.getPropertyValues();

            this.basePackage = updatePropertyValue("basePackage", values);
        }
        this.basePackage = Optional.ofNullable(this.basePackage).map(getEnvironment()::resolvePlaceholders).orElse(null);
    }

    @SuppressWarnings("SameParameterValue")
    private String updatePropertyValue(String propertyName, PropertyValues values) {
        PropertyValue property = values.getPropertyValue(propertyName);

        if (property == null) {
            return null;
        }
        Object value = property.getValue();
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof TypedStringValue) {
            return ((TypedStringValue) value).getValue();
        } else {
            return null;
        }
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public boolean isProcessPropertyPlaceHolders() {
        return processPropertyPlaceHolders;
    }

    public void setProcessPropertyPlaceHolders(boolean processPropertyPlaceHolders) {
        this.processPropertyPlaceHolders = processPropertyPlaceHolders;
    }

    public AnnotationAttributes getAnnotationAttributes() {
        return annotationAttributes;
    }

    public void setAnnotationAttributes(AnnotationAttributes annotationAttributes) {
        this.annotationAttributes = annotationAttributes;
    }
}

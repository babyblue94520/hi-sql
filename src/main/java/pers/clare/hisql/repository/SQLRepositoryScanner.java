package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import pers.clare.hisql.method.SQLProxyFactory;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.support.ResultSetConverter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SQLRepositoryScanner extends ClassPathBeanDefinitionScanner {
    private static final Logger log = LogManager.getLogger();
    private static final Set<String> repositoryNameSet = new HashSet<>();
    private static final Set<Class<?>> repositoryClassSet = new HashSet<>();

    static {
        repositoryNameSet.add(SQLRepository.class.getName());
        repositoryNameSet.add(SQLCrudRepository.class.getName());
        repositoryClassSet.add(SQLRepository.class);
        repositoryClassSet.add(SQLCrudRepository.class);
    }

    private final ClassLoader classLoader;
    private final AnnotationAttributes annotationAttributes;

    private final BeanDefinitionRegistry registry;

    public SQLRepositoryScanner(
            BeanDefinitionRegistry registry
            , ClassLoader classLoader
            , AnnotationAttributes annotationAttributes
    ) {
        super(registry);
        this.registry = registry;
        this.classLoader = classLoader;
        this.annotationAttributes = annotationAttributes;
    }

    public void registerFilters() {
        addIncludeFilter(new InterfaceTypeFilter(SQLRepository.class));
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            log.warn("No SQLRepository was found in '{}' package. Please check your configuration.", Arrays.toString(basePackages));
        } else {
            try {
                processBeanDefinitions(beanDefinitions);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        if (repositoryNameSet.contains(beanDefinition.getBeanClassName())) return false;

        AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
        if (!isRepository(annotationMetadata)) return false;

        String[] interfaceNames = annotationMetadata.getInterfaceNames();
        try {
            for (String interfaceName : interfaceNames) {
                if (repositoryNameSet.contains(interfaceName)) return true;
                if (isCandidateInterfaces(this.classLoader.loadClass(interfaceName))) return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isRepository(AnnotationMetadata annotationMetadata) {
        for (MergedAnnotation<Annotation> annotation : annotationMetadata.getAnnotations()) {
            if (annotation.getType().getName().equals(Repository.class.getName())) return true;
        }
        return false;
    }

    private boolean isCandidateInterfaces(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            if (repositoryClassSet.contains(anInterface)) return true;
            if (isCandidateInterfaces(anInterface)) return true;
        }
        return false;
    }

    @Override
    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidates = super.findCandidateComponents(basePackage);
        for (BeanDefinition candidate : candidates) {
            if (candidate instanceof AnnotatedBeanDefinition) {
                AnnotationConfigUtils.processCommonDefinitionAnnotations((AnnotatedBeanDefinition) candidate);
            }
        }
        return candidates;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String beanName = registerSQLStoreService();
        GenericBeanDefinition definition = null;
        Class<? extends SQLRepositoryFactoryBean> factoryBeanClass = annotationAttributes.getClass("factoryBean");
        for (BeanDefinitionHolder holder : beanDefinitions) {
            String beanClassName = definition.getBeanClassName();
            log.debug("Creating SQLRepositoryFactoryBean with name '{}' and '{}' interface", holder.getBeanName(), beanClassName);
            if (beanClassName == null) {
                log.error("beanClassName is null.");
            } else {
                try {
                    Class<?> clazz = this.classLoader.loadClass(beanClassName);
                    ConstructorArgumentValues constructorArgumentValues = definition.getConstructorArgumentValues();
                    constructorArgumentValues.addGenericArgumentValue(clazz);
                    constructorArgumentValues.addGenericArgumentValue(beanName);
                    definition.setBeanClass(factoryBeanClass);
                    definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                } catch (ClassNotFoundException e) {
                    log.error(e);
                }
            }
        }
    }

    private String registerSQLStoreService() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String prefix = annotationAttributes.getString("beanNamePrefix");
        if (StringUtils.hasLength(prefix)) {
            prefix = "HiSql#";
        }
        String beanName = prefix + SQLStoreService.class.getSimpleName();
        String dataSourceName = annotationAttributes.getString("dataSourceRef");
        String xmlRootPath = annotationAttributes.getString("xmlRootPath");
        Class<? extends NamingStrategy> namingClass = annotationAttributes.getClass("naming");
        Class<? extends PaginationMode> paginationModeClass = annotationAttributes.getClass("paginationMode");
        Class<? extends ResultSetConverter> resultSetConverter = annotationAttributes.getClass("resultSetConverter");

        BeanDefinitionBuilder sqlStoreServiceBuilder = BeanDefinitionBuilder.genericBeanDefinition(SQLStoreService.class);
        if (dataSourceName.length() == 0) {
            sqlStoreServiceBuilder.addAutowiredProperty("dataSource");
        } else {
            sqlStoreServiceBuilder.addPropertyReference("dataSource", dataSourceName);
        }
        sqlStoreServiceBuilder.addPropertyValue("xmlRoot", xmlRootPath);

        sqlStoreServiceBuilder.addPropertyValue("paginationMode", paginationModeClass.getConstructor().newInstance());

        sqlStoreServiceBuilder.addPropertyValue("naming", namingClass.getConstructor().newInstance());

        sqlStoreServiceBuilder.addPropertyValue("resultSetConverter", resultSetConverter.getConstructor().newInstance());

        registry.registerBeanDefinition(beanName, sqlStoreServiceBuilder.getBeanDefinition());
        return beanName;

    }

    private static class InterfaceTypeFilter extends AssignableTypeFilter {
        public InterfaceTypeFilter(Class<?> targetType) {
            super(targetType);
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                throws IOException {

            return metadataReader.getClassMetadata().isInterface() && super.match(metadataReader, metadataReaderFactory);
        }
    }
}

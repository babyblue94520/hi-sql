package pers.clare.hisql.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import pers.clare.hisql.service.SQLStoreService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class SQLRepositoryScanner extends ClassPathBeanDefinitionScanner {
    private static final Logger log = LogManager.getLogger();

    private final AnnotationAttributes annotationAttributes;
    private final SQLStoreService sqlStoreService;

    public SQLRepositoryScanner(
            BeanDefinitionRegistry registry
            , AnnotationAttributes annotationAttributes
            , SQLStoreService sqlStoreService
    ) {
        super(registry);
        this.annotationAttributes = annotationAttributes;
        this.sqlStoreService = sqlStoreService;
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
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        if (SQLRepository.class.getName().equals(beanDefinition.getBeanClassName())
                || SQLCrudRepository.class.getName().equals(beanDefinition.getBeanClassName())) {
            return false;
        }
        AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
        String[] interfaceNames = annotationMetadata.getInterfaceNames();
        for (String interfaceName : interfaceNames) {
            if (SQLRepository.class.getName().equals(interfaceName)
                    || SQLCrudRepository.class.getName().equals(interfaceName)) {
                return true;
            }
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

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        Class<? extends SQLRepositoryFactoryBean> factoryBeanClass = annotationAttributes.getClass("factoryBean");
        for (BeanDefinitionHolder holder : beanDefinitions) {
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String beanClassName = definition.getBeanClassName();
            log.debug("Creating SQLRepositoryFactoryBean with name '{}' and '{}' interface", holder.getBeanName(), beanClassName);
            if (beanClassName == null) {
                log.warn("beanClassName is null.");
            } else {
                definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
                definition.getConstructorArgumentValues().addGenericArgumentValue(this.sqlStoreService);
                definition.setBeanClass(factoryBeanClass);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            }
        }
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

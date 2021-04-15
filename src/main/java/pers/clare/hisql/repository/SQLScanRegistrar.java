package pers.clare.hisql.repository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import pers.clare.hisql.annotation.EnableHiSql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLScanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    /**
     * {@inheritDoc}
     *
     * @deprecated Since 2.0.2, this method not used never.
     */
    @Override
    @Deprecated
    public void setResourceLoader(ResourceLoader resourceLoader) {
        // NOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes SQLEntityScanAttrs = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableHiSql.class.getName()));
        if (SQLEntityScanAttrs != null) {
            registerBeanDefinitions(importingClassMetadata, SQLEntityScanAttrs, registry,
                    generateBaseBeanName(importingClassMetadata));
        }
    }

    void registerBeanDefinitions(
            AnnotationMetadata annotationMetadata
            , AnnotationAttributes annotationAttributes
            , BeanDefinitionRegistry registry
            , String beanName
    ) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SQLScanner.class);
        builder.addPropertyValue("annotationAttributes", annotationAttributes);

        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(
                Arrays.stream(annotationAttributes.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));

        basePackages.addAll(Arrays.stream(annotationAttributes.getStringArray("basePackages")).filter(StringUtils::hasText)
                .collect(Collectors.toList()));

        basePackages.addAll(Arrays.stream(annotationAttributes.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName)
                .collect(Collectors.toList()));

        if (basePackages.isEmpty()) {
            basePackages.add(getDefaultBasePackage(annotationMetadata));
        }
        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());

    }

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata) {
        return importingClassMetadata.getClassName() + "#" + SQLScanRegistrar.class.getSimpleName();
    }

    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }
}

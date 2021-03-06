package pers.clare.hisql.repository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import pers.clare.hisql.annotation.EnableHiSql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLScanRegistrar implements ImportBeanDefinitionRegistrar {

    private static String generateBaseBeanName(AnnotationMetadata importingClassMetadata) {
        return importingClassMetadata.getClassName() + "#" + SQLScanner.class.getSimpleName();
    }

    private static String getDefaultBasePackage(AnnotationMetadata importingClassMetadata) {
        return ClassUtils.getPackageName(importingClassMetadata.getClassName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes
                .fromMap(importingClassMetadata.getAnnotationAttributes(EnableHiSql.class.getName()));
        if (attributes != null) {
            registerBeanDefinitions(importingClassMetadata, attributes, registry);
        }
    }

    void registerBeanDefinitions(
            AnnotationMetadata annotationMetadata
            , AnnotationAttributes annotationAttributes
            , BeanDefinitionRegistry registry
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
        String beanName = generateBaseBeanName(annotationMetadata);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }
}

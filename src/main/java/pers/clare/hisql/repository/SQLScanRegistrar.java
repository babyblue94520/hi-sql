package pers.clare.hisql.repository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import pers.clare.hisql.annotation.EnableHiSql;
import pers.clare.hisql.naming.NamingStrategy;
import pers.clare.hisql.page.PaginationMode;
import pers.clare.hisql.service.SQLStoreService;
import pers.clare.hisql.support.ResultSetConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLScanRegistrar implements ImportBeanDefinitionRegistrar {

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
        if (attributes == null) return;

        try {
            registerBeanDefinitions(importingClassMetadata, attributes, registry);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    void registerBeanDefinitions(
            AnnotationMetadata annotationMetadata
            , AnnotationAttributes annotationAttributes
            , BeanDefinitionRegistry registry
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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
        builder.addPropertyValue("serviceName", registerSQLStoreService(annotationMetadata, annotationAttributes, registry));

        StringBuilder beanName = new StringBuilder(annotationAttributes.getString("beanNamePrefix"));
        if (beanName.length() == 0) {
            beanName.append(annotationMetadata.getClassName())
                    .append('#');
        }

        beanName.append(SQLScanner.class.getSimpleName());
        registry.registerBeanDefinition(beanName.toString(), builder.getBeanDefinition());
    }


    private String registerSQLStoreService(
            AnnotationMetadata annotationMetadata
            , AnnotationAttributes annotationAttributes
            , BeanDefinitionRegistry registry
    ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
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

        StringBuilder beanName = new StringBuilder(annotationAttributes.getString("beanNamePrefix"));
        if (beanName.length() == 0) {
            beanName.append(annotationMetadata.getClassName())
                    .append('#');
        }

        beanName.append(SQLStoreService.class.getSimpleName());
        registry.registerBeanDefinition(beanName.toString(), sqlStoreServiceBuilder.getBeanDefinition());
        return beanName.toString();
    }
}

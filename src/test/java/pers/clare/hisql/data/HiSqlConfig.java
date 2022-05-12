package pers.clare.hisql.data;

import pers.clare.hisql.annotation.EnableHiSql;

@EnableHiSql(
        basePackages = "pers.clare.hisql.data.repository"
        , dataSourceRef = "dataSource"
//        , dataSourceRef = HiSqlConfig.DATA_SOURCE
)
public class HiSqlConfig {
    public static final String PREFIX = "demo";
    static final String DATA_SOURCE_PROPERTIES = "spring.datasource." + PREFIX+".hikari";

    public static final String DATA_SOURCE = PREFIX + "DataSource";
    public static final String DATA_SOURCE_INITIALIZER = PREFIX + "DataSourceInitializer";

//    @Primary
//    @Bean
//    @ConfigurationProperties(prefix = DATA_SOURCE_PROPERTIES)
//    public DataSourceProperties dataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Primary
//    @Bean(name = DATA_SOURCE)
//    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
//        return dataSourceProperties.initializeDataSourceBuilder().build();
//    }


//    @Primary
//    @Bean(name = DATA_SOURCE_INITIALIZER)
//    public DataSourceInitializer dataSourceInitializer(
//            @Qualifier(DATA_SOURCE) DataSource datasource
////            DataSource datasource
//            , @Value("${" + DATA_SOURCE_PROPERTIES + ".hikari.schema}") String schema
//    ) {
//        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
//        dataSourceInitializer.setDataSource(datasource);
//        if (!StringUtils.isEmpty(schema)) {
//            ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
//            resourceDatabasePopulator.addScript(new ClassPathResource(schema));
//            dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
//        }
//        return dataSourceInitializer;
//    }
}

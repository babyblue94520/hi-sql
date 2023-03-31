package pers.clare.hisql.performance.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = {"pers.clare.hisql.performance.jpa"}
)
public class JpaConfig {
}

package com.ibyte.framework.discovery.core;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * JPA 配置
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @since 1.0.1
 */
@ConditionalOnMissingBean(DataSource.class)
@Configuration
@EnableJpaRepositories(basePackages = { NamingConstant.BASE_PACKAGE })
@EntityScan(basePackages = { NamingConstant.BASE_PACKAGE })
public class PluginJpaConfig {
    private final static String DB_META = NamingConstant.BASE_PACKAGE
            + ".resource.dbmeta";

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        dataSource.setConnectionTestQuery(
                getTestQuery(properties.getDriverClassName()));
        return dataSource;
    }

    private String getTestQuery(String driverClassName) {
        ResourceBundle bundle = ResourceBundle.getBundle(DB_META);
        try {
            return bundle.getString(StringHelper.join(driverClassName,
                    ".connection-test-query"));
        } catch (MissingResourceException e) {
            return bundle.getString("default.connection-test-query");
        }
    }
}

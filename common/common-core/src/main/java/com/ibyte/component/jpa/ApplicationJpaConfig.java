package com.ibyte.component.jpa;

import com.ibyte.common.constant.NamingConstant;
import com.ibyte.common.util.StringHelper;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @Description: <JPA配置更新>
 *
 * @author <a href="mailto:shangzhi.ibyte@gmail.com">iByte</a>
 * @Date: 2019-10-29
 */
@Configuration
@EnableJpaRepositories(basePackages = { NamingConstant.BASE_PACKAGE })
@EntityScan(basePackages = { NamingConstant.BASE_PACKAGE })
@EnableTransactionManagement
@EnableJpaAuditing
public class ApplicationJpaConfig {
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
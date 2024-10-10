package com.sk.customer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("com.sk.customer.persistence")
@Slf4j
public class DatasourceConfig {

     @Bean
     @Primary
     @ConfigurationProperties("spring.datasource")
     public DataSourceProperties dataSourceProperties() {
          return new DataSourceProperties();
     }

     @Bean
     public DataSource dataSource() throws SQLException {
          log.debug("Doing the datasource configuration setup");
          var dataSourceProperties = dataSourceProperties();
          var dataSource = new SimpleDriverDataSource();
          dataSource.setDriver(DriverManager.getDriver(dataSourceProperties.getUrl()));
          dataSource.setUrl(dataSourceProperties.getUrl());
          dataSource.setUsername(dataSourceProperties.getUsername());
          dataSource.setPassword(dataSourceProperties.getPassword());
          log.debug("Completed the datasource configuration setup");
          return dataSource;
     }
}

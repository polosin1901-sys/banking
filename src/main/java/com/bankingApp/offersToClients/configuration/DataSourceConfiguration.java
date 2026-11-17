package com.bankingApp.offersToClients.configuration;

import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Primary data source used by JPA and Liquibase (PostgreSQL with dynamic rules).
 *
 * <p>The application works with two databases, so one of them must be marked as primary to let
 * Spring Boot autoconfigure the entity manager and the migration tool without ambiguity.</p>
 */
@Configuration
public class DataSourceConfiguration {

    @Primary
    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}

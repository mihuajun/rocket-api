package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.PostgreSQLDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Properties;

/**
 * postgre SQL  构造器
 */
public class PostgreSQLFactory implements IDataSourceDialectFactory{

    @Override
    public String getName() {
        return "PostgreSQL";
    }

    @Override
    public String getIcon() {
        return "rocket-api/img/postgresql.icon";
    }

    @Override
    public DataSourceDialect factory(Properties properties) throws Exception {
        HikariDataSource dataSource = new HikariDataSource(new HikariConfig(properties));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new PostgreSQLDataSource(jdbcTemplate);
    }
}

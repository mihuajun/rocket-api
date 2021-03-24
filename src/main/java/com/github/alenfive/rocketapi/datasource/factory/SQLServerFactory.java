package com.github.alenfive.rocketapi.datasource.factory;


import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.SQLServerDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Properties;

/**
 * SQL  构造器
 */
public class SQLServerFactory implements IDataSourceDialectFactory{

    @Override
    public DataSourceDialect factory(Properties properties) throws Exception {
        HikariDataSource dataSource = new HikariDataSource(new HikariConfig(properties));
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new SQLServerDataSource(jdbcTemplate);
    }
}

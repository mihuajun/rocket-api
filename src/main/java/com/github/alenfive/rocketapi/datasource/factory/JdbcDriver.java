package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.entity.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class JdbcDriver extends IDataSourceDialectDriver {

    protected JdbcTemplate getJdbcTemplate(DBConfig config){
        HikariConfig hikariConfig = new HikariConfig(config.getProperties());
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        return new JdbcTemplate(dataSource);
    }

}

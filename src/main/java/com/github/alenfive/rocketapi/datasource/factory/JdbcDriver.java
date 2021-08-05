package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.entity.DBConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public abstract class JdbcDriver extends IDataSourceDialectDriver {

    protected DataSource getDataSource(DBConfig config){
        HikariConfig hikariConfig = new HikariConfig(config.getProperties());
        hikariConfig.setJdbcUrl(config.getUrl());
        hikariConfig.setUsername(config.getUser());
        hikariConfig.setPassword(config.getPassword());
        return new HikariDataSource(hikariConfig);
    }

}

package com.github.alenfive.rocketapi.datasource.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.SpringContextUtils;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.PostgreSQLDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * postgre SQL  构造器
 */
public class PostgreSQLFactory implements IDataSourceDialectFactory{

    @Override
    public DataSourceDialect factory(Map<String,Object> config) throws Exception {
        ObjectMapper objectMapper = SpringContextUtils.getApplicationContext().getBean(ObjectMapper.class);
        HikariDataSource dataSource = objectMapper.readValue(objectMapper.writeValueAsBytes(config),HikariDataSource.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new PostgreSQLDataSource(jdbcTemplate);
    }
}

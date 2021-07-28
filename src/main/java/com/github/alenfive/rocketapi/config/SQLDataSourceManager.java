package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

@ConditionalOnBean(JdbcTemplate.class)
public class SQLDataSourceManager extends DataSourceManager {

    @Autowired
    private SpringContextUtils contextUtils;

    @PostConstruct
    public void init() {

        Map<String,JdbcTemplate> jdbcTemplateMap = contextUtils.getContext().getBeansOfType(JdbcTemplate.class);
    }
}

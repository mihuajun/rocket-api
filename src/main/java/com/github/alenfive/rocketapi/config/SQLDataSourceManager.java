package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.MongoDataSource;
import com.github.alenfive.rocketapi.datasource.SqlDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/12/18 10:27
 * @UpdateDate: 2020/12/18 10:27
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
@ConditionalOnBean(JdbcTemplate.class)
public class SQLDataSourceManager extends DataSourceManager {

    @Autowired
    private SpringContextUtils contextUtils;

    @PostConstruct
    public void init() {

        Map<String,JdbcTemplate> jdbcTemplateMap = contextUtils.getContext().getBeansOfType(JdbcTemplate.class);
    }
}

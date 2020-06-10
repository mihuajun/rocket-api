package com.github.alenfive.dataway2.extend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 6/6/2020 4:28 PM
 * @UpdateDate: 6/6/2020 4:28 PM
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 默认数据源管理器
 */
@Component
public class DefaultDataSourceManager extends DataSourceManager {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {

        Map<String,DataSourceDialect> dialects = new HashMap<>();
        dialects.put("mysql",new MysqlDataSource(jdbcTemplate,true));
        dialects.put("mongodb",new MongoDataSource(mongoTemplate,false));
        super.setDialectMap(dialects);
    }
}

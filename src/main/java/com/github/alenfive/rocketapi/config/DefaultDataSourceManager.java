package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.datasource.*;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认数据源管理器
 */
@Component
public class DefaultDataSourceManager extends DataSourceManager {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {
        Map<String, DataSourceDialect> dialects = new LinkedHashMap<>();
        dialects.put("mysql",new MySQLDataSource(jdbcTemplate,false));
        //dialects.put("postgres",new PostgreSQLDataSource(jdbcTemplate,false));
        //dialects.put("oracle",new OracleDataSource(jdbcTemplate,true));
        dialects.put("mongodb",new MongoDataSource(mongoTemplate,true));
        super.setDialectMap(dialects);
    }
}

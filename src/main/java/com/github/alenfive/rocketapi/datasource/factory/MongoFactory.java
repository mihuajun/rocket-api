package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.MongoDataSource;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.Map;

/**
 * mongodb  构造器
 */
public class MongoFactory implements IDataSourceDialectFactory{

    @Override
    public DataSourceDialect factory(Map<String,Object> config) {

        String url = (String) config.get("url");
        MongoClientURI mongoclienturi = new MongoClientURI(url);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoclienturi);
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory);
        return new MongoDataSource(mongoTemplate);
    }
}

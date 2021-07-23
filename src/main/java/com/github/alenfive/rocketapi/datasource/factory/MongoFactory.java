package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.MongoDataSource;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.Properties;

/**
 * mongodb  构造器
 */
public class MongoFactory implements IDataSourceDialectFactory{

    @Override
    public String getName() {
        return "MongoDB";
    }

    @Override
    public String getIcon() {
        return "rocket-api/img/mongodb.icon";
    }

    @Override
    public DataSourceDialect factory(Properties properties) {

        String url = properties.getProperty("url");
        MongoClientURI mongoclienturi = new MongoClientURI(url);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoclienturi);
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory);
        return new MongoDataSource(mongoTemplate);
    }
}

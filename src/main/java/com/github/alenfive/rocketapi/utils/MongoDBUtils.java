package com.github.alenfive.rocketapi.utils;

import com.github.alenfive.rocketapi.entity.DBConfig;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

public class MongoDBUtils {
    public static MongoTemplate getMongoTemplate(DBConfig config){
        MongoClientURI mongoClientURI = new MongoClientURI(config.getUrl());
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClientURI);
        return new MongoTemplate(mongoDbFactory);
    }
}

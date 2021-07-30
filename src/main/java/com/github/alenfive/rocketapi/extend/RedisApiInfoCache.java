package com.github.alenfive.rocketapi.extend;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 集群通知
 */
public class RedisApiInfoCache implements IApiInfoCache{

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    private String buildPrefix(){
        return "rocket-api:"+rocketApiProperties.getServiceName();
    }

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return buildPrefix()+":"+apiInfo.getMethod() +"-"+ apiInfo.getFullPath();
    }

    @Override
    public ApiInfo get(ApiInfo apiInfo) {
        String strValue = redisTemplate.opsForValue().get(buildApiInfoKey(apiInfo));
        try {
            return objectMapper.readValue(strValue,ApiInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(ApiInfo apiInfo) {
        try {
            String strValue = objectMapper.writeValueAsString(apiInfo);
            redisTemplate.opsForValue().set(buildApiInfoKey(apiInfo),strValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(ApiInfo apiInfo) {
        redisTemplate.delete(buildApiInfoKey(apiInfo));
    }

    @Override
    public void removeAll() {
        redisTemplate.delete(getKeys());
    }

    private List<String> getKeys(){
        String patternKey = buildPrefix()+":*";
        ScanOptions options = ScanOptions.scanOptions()
                .count(10000)
                .match(patternKey).build();
        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
        List<String> keys = new ArrayList<>();
        while(cursor.hasNext()){
            keys.add(cursor.next().toString());
        }
        try {
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keys;
    }

    @Override
    public Collection<ApiInfo> getAll() {
        return redisTemplate.opsForValue().multiGet(getKeys()).stream().map(item->{
            try {
                return objectMapper.readValue(item,ApiInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }
}

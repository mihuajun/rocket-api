package com.github.alenfive.rocketapi.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUHashMap<String,Object> extends LinkedHashMap {

    private Integer maxCacheSize = null;

    private LRUHashMap(){

    }

    public LRUHashMap(Integer maxCacheSize){
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        if (size()>maxCacheSize){
            return true;
        }
        return false;
    }
}

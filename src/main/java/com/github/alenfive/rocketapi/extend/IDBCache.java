package com.github.alenfive.rocketapi.extend;

/**
 * 数据库缓存操作
 */
public interface IDBCache {
    public void set(String cacheKey,Object value,Long cacheTime);
    public Object get(String cacheKey);
    public void remove(String cacheKey);
    public void clear();
}

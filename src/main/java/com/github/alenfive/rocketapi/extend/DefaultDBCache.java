package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.LRUHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 默认数据库查询缓存操作
 */
@Component
@Slf4j
public class DefaultDBCache implements IDBCache{

    private Integer maxCacheSize = 1024;

    // 定时器
    private Timer timer = new Timer();

    //过期检测周期
    private static final long CHECK_TIME_SECOND = 10 * 1000;

    private LRUHashMap<String,Object> cacheMap = new LRUHashMap<>(maxCacheSize);
    private HashMap<String,Long> expiresTime = new HashMap<>();

    //过期处理
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Set<String> keys = expiresTime.keySet();
            for (String key : keys){
                if (expiresTime.get(key) <= System.currentTimeMillis()){
                    synchronized (this){
                        expiresTime.remove(key);
                        cacheMap.remove(key);
                        log.debug("Automatic cache expiration:{}",key);
                    }
                }
            }
        }
    };


    @PostConstruct
    public void init(){
        timer.schedule(timerTask, CHECK_TIME_SECOND,CHECK_TIME_SECOND);
    }

    @Override
    public void set(String cacheKey, Object value, Long cacheTime) {
        synchronized (this){
            this.cacheMap.put(cacheKey,value);
            this.expiresTime.put(cacheKey,System.currentTimeMillis()+cacheTime);
        }
    }

    @Override
    public Object get(String cacheKey) {
        return this.cacheMap.get(cacheKey);
    }

    @Override
    public void remove(String cacheKey) {
        synchronized (this){
            this.cacheMap.remove(cacheKey);
            this.expiresTime.remove(cacheKey);
        }
    }

    @Override
    public void clear() {
        synchronized (this){
            this.cacheMap.clear();
            this.expiresTime.clear();
        }
    }
}

package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API信息缓存
 */
@Component
public class DefaultApiInfoCache implements IApiInfoCache {

    private Map<String, ApiInfo> cacheApiInfo = new ConcurrentHashMap<>();

    private String identity;

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Override
    public ApiInfo get(ApiInfo apiInfo){
        return cacheApiInfo.get(buildApiInfoKey(apiInfo));
    }

    @Override
    public Collection<ApiInfo> getAll() {
        return cacheApiInfo.values();
    }

    @Override
    public void removeAll() {
        cacheApiInfo.clear();
    }

    @Override
    public void remove(ApiInfo apiInfo) {
        cacheApiInfo.remove(buildApiInfoKey(apiInfo));
    }

    @Override
    public void put(ApiInfo apiInfo) {
        cacheApiInfo.put(buildApiInfoKey(apiInfo),apiInfo);
    }

    @Override
    public void putAll(Collection<ApiInfo> apiInfos) {
        for (ApiInfo apiInfo : apiInfos){
            this.put(apiInfo);
        }
    }

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return apiInfo.getMethod() +" "+ apiInfo.getFullPath();
    }

    /**
     * 在页面触发"Rebuild API List"操作时，会触发此方法，可使用Redis消息通过功能重写该方法，
     * 以达到分布式环境下多实例部署更新问题
     */
    @Override
    public void refreshNotify() {
        identity = GenerateId.get().toHexString();
        this.receiveNotify(identity);
    }

    /**
     * 监听 "@refreshNotify"行为，来重载本地request mapping等本地实体行为的重新初始化
     * @param identity
     */
    @Override
    public void receiveNotify(String identity) {
        //避免本实例重复初始化
        if (identity.equals(this.identity)){
            return;
        }
        try {
            mappingFactory.buildInit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

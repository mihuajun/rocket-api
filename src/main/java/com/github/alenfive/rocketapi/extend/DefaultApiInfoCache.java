package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.utils.GenerateId;
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

    private String instanceId = GenerateId.get().toHexString();

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

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return apiInfo.getMethod() +" "+ apiInfo.getFullPath();
    }

}

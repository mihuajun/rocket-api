package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.vo.NotifyEntity;

import java.util.Collection;

/**
 * API信息缓存
 */
public interface IApiInfoCache {
    public ApiInfo get(ApiInfo apiInfo);
    public void put(ApiInfo apiInfo);
    public void remove(ApiInfo apiInfo);
    public void removeAll();
    public Collection<ApiInfo> getAll();
    public void refreshNotify(NotifyEntity notifyEntity);
    public void receiveNotify(String instanceId, NotifyEntity notifyEntity);
}

package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;

import java.util.List;

/**
 * API信息接口同步，
 */
public interface IApiSync {
    public void sync(List<ApiInfo> apiInfos);
}

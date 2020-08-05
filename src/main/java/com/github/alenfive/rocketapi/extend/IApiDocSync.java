package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiExample;
import com.github.alenfive.rocketapi.entity.ApiInfo;

/**
 * API信息接口同步，
 */
public interface IApiDocSync {
    public void sync(ApiInfo apiInfo, ApiExample apiExample);
}

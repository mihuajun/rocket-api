package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.stereotype.Component;

/**
 * @Description:参数变量域
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/6/23 21:26
 * @UpdateDate: 2020/6/23 21:26
 * @UpdateRemark: init
 * @Version: 1.0
 */

@Component
public class ApiInfoContent {

    private ThreadLocal<ApiInfo> apiInfo = new ThreadLocal<>();
    private ThreadLocal<ApiParams> apiParams = new ThreadLocal<>();

    public ApiInfo getApiInfo() {
        return apiInfo.get();
    }

    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo.set(apiInfo);
    }

    public ApiParams getApiParams() {
        return apiParams.get();
    }

    public void setApiParams(ApiParams apiParams) {
        this.apiParams.set(apiParams);
    }
}

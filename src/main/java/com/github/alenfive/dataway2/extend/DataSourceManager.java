package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 6/6/2020 4:26 PM
 * @UpdateDate: 6/6/2020 4:26 PM
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 数据源组
 */
public abstract class DataSourceManager extends DataSourceDialect{

    Map<String,DataSourceDialect> dialectMap;

    /**
     * 查询API存储的数据源
     * @return
     */
    public String getStoreApiKey(){
        return dialectMap.keySet().stream().filter(key->dialectMap.get(key).isStoreApi()).findFirst().orElse(null);
    }

    @Override
    public String listApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).listApiInfoScript();
    }
    @Override
    public String getApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).getApiInfoScript();
    }
    @Override
    public String saveApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).saveApiInfoScript();
    }
    @Override
    public String updateApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).updateApiInfoScript();
    }
    @Override
    public String deleteApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).deleteApiInfoScript();
    }

    @Override
    public Object execute(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return dialectMap.get(apiInfo.getDatasource()).execute(script,apiInfo,apiParams);
    }

    @Override
    public List<Map<String, Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return dialectMap.get(apiInfo.getDatasource()).executeQuery(script,apiInfo,apiParams);
    }

    @Override
    public Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return dialectMap.get(apiInfo.getDatasource()).executeCount(script,apiInfo,apiParams);
    }

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String, DataSourceDialect> dialectMap) {
        this.dialectMap = dialectMap;
    }
}

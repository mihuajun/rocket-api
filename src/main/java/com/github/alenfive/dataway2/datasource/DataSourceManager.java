package com.github.alenfive.dataway2.datasource;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.util.StringUtils;

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
    public String saveApiExampleScript() {
        return dialectMap.get(getStoreApiKey()).saveApiExampleScript();
    }
    @Override
    public String lastApiExampleScript() {
        return dialectMap.get(getStoreApiKey()).lastApiExampleScript();
    }
    @Override
    public String deleteExampleScript() {
        return dialectMap.get(getStoreApiKey()).deleteExampleScript();
    }

    @Override
    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.find(script,apiInfo,apiParams,null);
    }

    @Override
    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.update(script,apiInfo,apiParams,null);
    }

    @Override
    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.remove(script,apiInfo,apiParams,null);
    }

    @Override
    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams)throws Exception {
        return this.insert(script,apiInfo,apiParams,null);
    }

    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.find(script,apiInfo,apiParams);
    }

    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.remove(script,apiInfo,apiParams);
    }

    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.insert(script,apiInfo,apiParams);
    }

    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.update(script,apiInfo,apiParams);
    }

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String, DataSourceDialect> dialectMap) {
        this.dialectMap = dialectMap;
    }

    private DataSourceDialect buildDataSourceDialect(String defaultDataSource,String specifyDataSource){
        String dataSourceKey = StringUtils.isEmpty(specifyDataSource)?defaultDataSource:specifyDataSource;
        DataSourceDialect dataSourceDialect = dialectMap.get(dataSourceKey);
        if (dataSourceDialect == null){
            throw new IllegalArgumentException("unknown data source "+dataSourceKey);
        }
        return dataSourceDialect;
    }
}

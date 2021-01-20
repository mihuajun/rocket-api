package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理器代理类，实现指定数据源操作的路由
 */
public abstract class DataSourceManager {

    private Map<String,DataSourceDialect> dialectMap;

    private ScriptParseService parseService;

    public void setParseService(ScriptParseService parseService) {
        this.parseService = parseService;
    }

    /**
     * 查询API存储的数据源
     */
    public String getStoreApiKey(){
        return dialectMap.keySet().stream().filter(key->dialectMap.get(key).isStoreApi()).findFirst().orElse(null);
    }
    public List<ApiInfo> listApiInfoByEntity(ApiInfo apiInfo){
        return dialectMap.get(getStoreApiKey()).listApiInfoByEntity(apiInfo);
    }

    public List<ApiInfoHistory> listApiInfoHistoryByEntity(ApiInfoHistory apiInfoHistory, IApiPager apiPager, Page page) {
        return dialectMap.get(getStoreApiKey()).listApiInfoHistoryByEntity(apiInfoHistory,apiPager,page);
    }

    public void saveApiInfoHistory(ApiInfoHistory apiInfoHistory) {
        dialectMap.get(getStoreApiKey()).saveApiInfoHistory(apiInfoHistory);
    }

    public void saveApiInfo(ApiInfo apiInfo){
        dialectMap.get(getStoreApiKey()).saveApiInfo(apiInfo);
    }
    public ApiInfo findApiInfoById(ApiInfo apiInfo){
       return dialectMap.get(getStoreApiKey()).findApiInfoById(apiInfo);
    }
    public void updateApiInfo(ApiInfo apiInfo){
        dialectMap.get(getStoreApiKey()).updateApiInfo(apiInfo);
    }
    public void deleteApiInfo(ApiInfo apiInfo){
        dialectMap.get(getStoreApiKey()).deleteApiInfo(apiInfo);
    }
    public void saveApiExample(ApiExample apiExample) {
        dialectMap.get(getStoreApiKey()).saveApiExample(apiExample);
    }
    public List<ApiExample> listApiExampleByEntity(ApiExample apiExample, IApiPager apiPager, Page page) {
        return dialectMap.get(getStoreApiKey()).listApiExampleByEntity(apiExample,apiPager,page);
    }
    public void deleteExample(ApiExample apiExample) {
        dialectMap.get(getStoreApiKey()).deleteExample(apiExample);
    }

    public void saveApiConfig(ApiConfig apiConfig){
        dialectMap.get(getStoreApiKey()).saveApiConfig(apiConfig);
    }
    public void updateApiConfig(ApiConfig apiConfig){
        dialectMap.get(getStoreApiKey()).updateApiConfig(apiConfig);
    }
    public List<ApiConfig> listApiConfigByEntity(ApiConfig apiConfig){
        return dialectMap.get(getStoreApiKey()).listApiConfigByEntity(apiConfig);
    }


    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource,Map<String,Object> specifyParams,IApiPager apiPager, Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        StringBuilder sb = new StringBuilder(script);
        parseService.parse(sb,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.buildCountScript(sb.toString(),apiInfo,apiParams,apiPager,page);
    }

    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams,IApiPager apiPager,Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        StringBuilder sb = new StringBuilder(script);
        parseService.parse(sb,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.buildPageScript(sb.toString(),apiInfo,apiParams,apiPager,page);
    }

    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.find(script,apiInfo,apiParams);
    }

    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.remove(script,apiInfo,apiParams);
    }

    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.insert(script,apiInfo,apiParams);
    }

    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect,specifyParams);
        return dataSourceDialect.update(script,apiInfo,apiParams);
    }

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String, DataSourceDialect> dialectMap) {
        this.dialectMap = dialectMap;
        if (this.getStoreApiKey() == null){
            throw new IllegalArgumentException("storeApi is not found");
        }
    }

    public DataSourceDialect buildDataSourceDialect(String defaultDataSource,String specifyDataSource){
        String dataSourceKey = StringUtils.isEmpty(specifyDataSource)?defaultDataSource:specifyDataSource;
        DataSourceDialect dataSourceDialect = dialectMap.get(dataSourceKey);
        if (dataSourceDialect == null){
            throw new IllegalArgumentException("unknown datasource `"+dataSourceKey+"`");
        }
        return dataSourceDialect;
    }
}

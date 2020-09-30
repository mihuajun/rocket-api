package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public String listApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).listApiInfoScript();
    }

    public String listApiInfoHistoryScript() {
        return dialectMap.get(getStoreApiKey()).listApiInfoHistoryScript();
    }

    public String saveApiInfoHistoryScript() {
        return dialectMap.get(getStoreApiKey()).saveApiInfoHistoryScript();
    }

    public String saveApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).saveApiInfoScript();
    }
    public String updateApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).updateApiInfoScript();
    }
    public String deleteApiInfoScript(){
        return dialectMap.get(getStoreApiKey()).deleteApiInfoScript();
    }
    public String saveApiExampleScript() {
        return dialectMap.get(getStoreApiKey()).saveApiExampleScript();
    }
    public String listApiExampleScript() {
        return dialectMap.get(getStoreApiKey()).listApiExampleScript();
    }
    public String deleteExampleScript() {
        return dialectMap.get(getStoreApiKey()).deleteExampleScript();
    }

    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.find(script,apiInfo,apiParams,null);
    }

    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.update(script,apiInfo,apiParams,null);
    }

    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return this.remove(script,apiInfo,apiParams,null);
    }

    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams)throws Exception {
        return this.insert(script,apiInfo,apiParams,null);
    }

    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams,IApiPager apiPager, Page page) throws Exception {
        return this.buildCountScript(script,apiInfo,apiParams,null,apiPager,page);
    }

    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource,IApiPager apiPager, Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.buildCountScript(script,apiInfo,apiParams,apiPager,page);
    }

    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,IApiPager apiPager, Page page) throws Exception {
        return this.buildPageScript(script,apiInfo,apiParams,null,apiPager,page);
    }

    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,IApiPager apiPager,Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        return dataSourceDialect.buildPageScript(script,apiInfo,apiParams,apiPager,page);
    }

    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect);
        return dataSourceDialect.find(script,apiInfo,apiParams);
    }

    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect);
        return dataSourceDialect.remove(script,apiInfo,apiParams);
    }

    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect);
        return dataSourceDialect.insert(script,apiInfo,apiParams);
    }

    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,apiParams,dataSourceDialect);
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
            throw new IllegalArgumentException("unknown data source "+dataSourceKey);
        }
        return dataSourceDialect;
    }
}

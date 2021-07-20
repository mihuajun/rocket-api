package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
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

    private DataSourceDialect storeApiDataSource;

    public void setParseService(ScriptParseService parseService) {
        this.parseService = parseService;
    }

    /**
     * 获取api存储的数据源
     * @return
     */
    public DataSourceDialect getStoreApiDataSource(){
        return storeApiDataSource;
    }

    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource,Map<String,Object> specifyParams,IApiPager apiPager, Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        StringBuilder sb = new StringBuilder(script);
        parseService.parse(sb,dataSourceDialect,specifyParams);
        return dataSourceDialect.buildCountScript(sb.toString(),apiInfo,apiParams,apiPager,page);
    }

    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams,IApiPager apiPager,Page page) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        StringBuilder sb = new StringBuilder(script);
        parseService.parse(sb,dataSourceDialect,specifyParams);
        return dataSourceDialect.buildPageScript(sb.toString(),apiInfo,apiParams,apiPager,page);
    }

    public List<Map<String, Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams, String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,dataSourceDialect,specifyParams);
        return dataSourceDialect.find(script,apiInfo,apiParams);
    }

    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,dataSourceDialect,specifyParams);
        return dataSourceDialect.remove(script,apiInfo,apiParams);
    }

    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,dataSourceDialect,specifyParams);
        return dataSourceDialect.insert(script,apiInfo,apiParams);
    }

    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams,String specifyDataSource,Map<String,Object> specifyParams) throws Exception {
        DataSourceDialect dataSourceDialect = buildDataSourceDialect(apiInfo.getDatasource(),specifyDataSource);
        parseService.parse(script,dataSourceDialect,specifyParams);
        return dataSourceDialect.update(script,apiInfo,apiParams);
    }

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String, DataSourceDialect> dialectMap) {
        this.dialectMap = dialectMap;
        String storeApiKey = dialectMap.keySet().stream().filter(key->dialectMap.get(key).isStoreApi()).findFirst()
                .orElseThrow(()->new IllegalArgumentException("storeApi is not found"));
        this.storeApiDataSource = dialectMap.get(storeApiKey);
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

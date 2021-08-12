package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.util.StringUtils;

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

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String,DataSourceDialect> dialectMap) {
        String storeApiName = dialectMap.keySet().stream().filter(key->dialectMap.get(key).isStoreApi()).findFirst().orElseThrow(()->new IllegalArgumentException("storeApi is not found"));
        this.storeApiDataSource = dialectMap.get(storeApiName);
        this.dialectMap = dialectMap;
    }

    public ScriptContext buildScriptContext(StringBuilder script,DataSourceDialect dataSourceDialect,Map<String,Object> params){
        return ScriptContext.builder()
                .script(script)
                .dataSourceDialect(dataSourceDialect)
                .params(parseService.parse(script,dataSourceDialect,params))
                .build();
    }

    public DataSourceDialect getDataSourceDialect(String datasource, String specifyDataSource){
        String dataSourceKey = StringUtils.isEmpty(specifyDataSource)?datasource:specifyDataSource;
        DataSourceDialect dataSourceDialect = this.dialectMap.get(dataSourceKey);
        if (dataSourceDialect == null){
            throw new IllegalArgumentException("unknown datasource `"+dataSourceKey+"`");
        }

        return dataSourceDialect;
    }
}

package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.util.StringUtils;

import java.util.HashMap;
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

    public Map<String, DataSourceDialect> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String,DataSourceDialect> dialectMap) {
        String storeApiName = dialectMap.keySet().stream().filter(key->dialectMap.get(key).isStoreApi()).findFirst().orElseThrow(()->new IllegalArgumentException("storeApi is not found"));
        this.storeApiDataSource = dialectMap.get(storeApiName);
        this.dialectMap = dialectMap;
    }

    public ScriptContext buildScriptContext(StringBuilder script, DataSourceDialect dataSourceDialect, Map<String,Object> params){
        return ScriptContext.builder()
                .script(script)
                .dataSourceDialect(dataSourceDialect)
                .params(new Map[]{parseService.parse(script,dataSourceDialect,params)})
                .build();
    }

    /**
     * 批量脚本构建，要求：
     * 1. 仅支持jdbc数据源
     * 2. 要求变量名以":varname" 形式定义
     * @param script
     * @param dataSourceDialect
     * @param params
     * @return
     */
    public ScriptContext buildScriptContext(StringBuilder script, DataSourceDialect dataSourceDialect, List<Map<String,Object>> params){
        if (!(dataSourceDialect instanceof JdbcDataSource)){
            throw new UnsupportedOperationException("Only JDBC data sources are supported");
        }
        return ScriptContext.builder()
                .script(script)
                .dataSourceDialect(dataSourceDialect)
                .params(params.toArray(new HashMap[params.size()]))
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

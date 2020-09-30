package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiExample;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiInfoHistory;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.FieldUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据源方言抽象类
 */
public abstract class DataSourceDialect {

    protected boolean storeApi = false;
    public boolean isStoreApi() {
        return storeApi;
    }



    abstract String saveApiInfoScript();
    abstract String deleteApiInfoScript();
    abstract String updateApiInfoScript();
    abstract String listApiInfoScript();

    abstract String saveApiInfoHistoryScript();
    abstract String listApiInfoHistoryScript();

    abstract String saveApiExampleScript();
    abstract String listApiExampleScript();
    abstract String deleteExampleScript();

    //查询对象
    abstract List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception;

    //返回影响的行数
    abstract Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception;

    //返回影响的行数
    abstract Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception;

    //返回主键
    abstract Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception;

    /**
     * 替换key
     */
    protected Map<String,Object> toReplaceKeyLow(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            result.put(FieldUtils.underlineToCamel(key.toLowerCase()),map.get(key));
        }
        return result;
    }

    public abstract String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page);

    public abstract String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page);

    //入参转码
    public abstract String transcoding(String param);

    public abstract List<TableInfo> buildTableInfo();
}

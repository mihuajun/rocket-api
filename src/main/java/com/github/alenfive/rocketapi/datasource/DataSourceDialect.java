package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.extend.IPagerDialect;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源方言抽象类
 */
public abstract class DataSourceDialect {

    protected boolean storeApi = false;

    public boolean isStoreApi() {
        return storeApi;
    }

    abstract String listApiInfoScript();
    abstract String lastApiInfoHistoryScript();
    abstract String saveApiInfoHistoryScript();
    abstract String getApiInfoScript();
    abstract String saveApiInfoScript();
    abstract String updateApiInfoScript();
    abstract String deleteApiInfoScript();
    abstract String saveApiExampleScript();
    abstract String lastApiExampleScript();
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
     * @param map
     * @return
     */
    protected Map<String,Object> toReplaceKeyLow(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            result.put(underlineToCamel(key),map.get(key));
        }
        return result;
    }

    /**
     * 下划线转驼峰
     * @param name
     * @return
     */
    protected String underlineToCamel(String name){
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                if (++i < name.length()){
                    sb.append(Character.toUpperCase(name.charAt(i)));
                }
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }


    abstract String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Collection<IPagerDialect> pagerDialects) throws Exception;

    abstract String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Collection<IPagerDialect> pagerDialects) throws Exception;
}

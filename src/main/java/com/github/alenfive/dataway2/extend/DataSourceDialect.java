package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/27 17:16
 * @UpdateDate: 2020/5/27 17:16
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 数据源方言抽象类
 */
public abstract class DataSourceDialect {

    protected boolean storeApi = false;

    public boolean isStoreApi() {
        return storeApi;
    }

    abstract String listApiInfoScript();
    abstract String getApiInfoScript();
    abstract String saveApiInfoScript();
    abstract String updateApiInfoScript();
    abstract String deleteApiInfoScript();

    abstract Object execute(String script, ApiInfo apiInfo, ApiParams apiParams);

    abstract List<Map<String,Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams);

    abstract Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams);

    protected Map<String,Object> toReplaceKeyLow(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            result.put(underlineToCamel(key),map.get(key));
        }
        return result;
    }

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
}

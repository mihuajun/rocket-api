package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;

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
public interface DataSourceDialect {

    String listApiInfoScript();
    String getApiInfoScript();
    String saveApiInfoScript();
    String updateApiInfoScript();
    String deleteApiInfoScript();

    Object execute(String script, ApiInfo apiInfo, ApiParams apiParams);

    List<Map<String,Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams);

    Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams);

}

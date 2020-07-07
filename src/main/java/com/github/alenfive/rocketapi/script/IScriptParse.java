package com.github.alenfive.rocketapi.script;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;

/**
 * @Description: 脚本执行器
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/28 14:30
 * @UpdateDate: 2020/6/28 14:30
 * @UpdateRemark: init
 * @Version: 1.0
 */
public interface IScriptParse {
    public Object runScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Throwable;
}

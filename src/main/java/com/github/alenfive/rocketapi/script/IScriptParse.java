package com.github.alenfive.rocketapi.script;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;

import javax.script.Bindings;

/**
 * 脚本执行器接口，实现此接口可自定义脚本执行引擎
 */
public interface IScriptParse {
    public Object runScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Throwable;

    public Object engineEval(String script, Bindings bindings) throws Throwable;
}

package com.github.alenfive.dataway2.script;

/**
 * @Description: Groovy脚本执行器
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/28 14:31
 * @UpdateDate: 2020/6/28 14:31
 * @UpdateRemark: init
 * @Version: 1.0
 */

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import com.github.alenfive.dataway2.extend.ApiInfoContent;
import com.github.alenfive.dataway2.function.IFunction;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.swing.StringUIClientPropertyKey;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;

@Component
public class GroovyScriptParse implements IScriptParse{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ApplicationContext context;

    private Collection<IFunction> functionList;

    @PostConstruct
    public void init(){
        //加载函数
        functionList = context.getBeansOfType(IFunction.class).values();
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public Object runScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Throwable {

        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("groovy");

            //注入变量
            apiInfoContent.setApiInfo(apiInfo);
            apiInfoContent.setApiParams(apiParams);
            apiInfoContent.setEngine(engine);

            for(IFunction function : functionList){
                engine.put(function.getVarName(),function);
            }

            //注入属性变量
            buildScriptParams(engine,apiParams);
            Object result = engine.eval(script.toString());
            if (!(result instanceof ScriptObjectMirror)){
                return result;
            }
            ScriptObjectMirror som = (ScriptObjectMirror)result ;
            if (som.isArray()){
                return som.values();
            }
            return result;
        }catch (Exception e){
            if (e.getCause() != null && e.getCause().getCause() != null){
                throw e.getCause().getCause();
            }else{
                throw e;
            }
        }

    }

    private void buildScriptParams(ScriptEngine engine, ApiParams apiParams) {
        engine.put("pathVar",apiParams.getPathVar());
        engine.put("param",apiParams.getParam());
        engine.put("body",apiParams.getBody());
        engine.put("header",apiParams.getHeader());
        engine.put("cookie",apiParams.getCookie());
        engine.put("session",apiParams.getSession());

        if (!CollectionUtils.isEmpty(apiParams.getSession())){
            apiParams.getSession().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getCookie())){
            apiParams.getCookie().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getHeader())){
            apiParams.getHeader().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getBody())){
            apiParams.getBody().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getParam())){
            apiParams.getParam().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getPathVar())){
            apiParams.getPathVar().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                engine.put(key,value);
            });
        }

    }
}

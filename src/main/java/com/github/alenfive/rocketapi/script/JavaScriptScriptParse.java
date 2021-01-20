package com.github.alenfive.rocketapi.script;

/**
 * js脚本执行器
 */

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.function.IFunction;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Collection;


public class JavaScriptScriptParse implements IScriptParse{

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
    @Transactional
    public Object runScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Throwable {
        try {
            //注入变量
            apiInfoContent.setApiInfo(apiInfo);
            apiInfoContent.setApiParams(apiParams);

            //注入函数
            StringBuilder scriptContent = new StringBuilder();

            scriptContent.append("function run(){");
            scriptContent.append(script);
            scriptContent.append("}");
            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("js");

            for(IFunction function : functionList){
                engine.put(function.getVarName(),function);
            }

            //注入属性变量
            buildScriptParams(engine,apiParams);

            engine.eval(scriptContent.toString());
            Invocable inv = (Invocable) engine;
            Object result = inv.invokeFunction("run");
            if (!(result instanceof ScriptObjectMirror)){
                return result;
            }
            ScriptObjectMirror som = (ScriptObjectMirror)result ;
            if (som.isArray()){
                return som.values();
            }
            return som;
        }catch (Exception e){
            if (e.getCause() != null && e.getCause().getCause() != null){
                throw e.getCause().getCause();
            }else{
                throw e;
            }
        }
    }

    @Override
    public Object engineEval(String script, Bindings bindings) throws Throwable {
        return null;
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
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getCookie())){
            apiParams.getCookie().forEach((key,value)->{
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getHeader())){
            apiParams.getHeader().forEach((key,value)->{
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getBody())){
            apiParams.getBody().forEach((key,value)->{
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getParam())){
            apiParams.getParam().forEach((key,value)->{
                engine.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getPathVar())){
            apiParams.getPathVar().forEach((key,value)->{
                engine.put(key,value);
            });
        }

    }
}

package com.github.alenfive.rocketapi.script;

/**
 * Groovy脚本执行器
 */

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.function.IFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.util.Collection;
import java.util.regex.Pattern;

@Component
public class GroovyScriptParse implements IScriptParse{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ApplicationContext context;

    private Collection<IFunction> functionList;

    private ScriptEngineManager factory = new ScriptEngineManager();

    private ScriptEngine engine = null;

    private Pattern selectSqlPattern = Pattern.compile("^(\\s*select\\s+)", Pattern.CASE_INSENSITIVE);
    private Pattern insertSqlPattern = Pattern.compile("^(\\s*(replace|insert)\\s+into\\s+)", Pattern.CASE_INSENSITIVE);
    private Pattern updateSqlPattern = Pattern.compile("^(\\s*update\\s+[A-Za-z\\-0-9_]+\\s+set )", Pattern.CASE_INSENSITIVE);
    private Pattern deleteSqlPattern = Pattern.compile("^(\\s*delete\\s+from\\s+[A-Za-z\\-0-9_]+)", Pattern.CASE_INSENSITIVE);

    @PostConstruct
    public void init(){
        //初始化引擎
        engine = factory.getEngineByName("groovy");

        //加载函数
        functionList = context.getBeansOfType(IFunction.class).values();
    }

    @Override
    @Transactional(rollbackFor=Exception.class)
    public Object runScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Throwable {

        try {

            script = buildSQLScript(script,apiInfo);

            //注入变量
            apiInfoContent.setApiInfo(apiInfo);
            apiInfoContent.setApiParams(apiParams);

            Bindings bindings = new SimpleBindings();

            apiInfoContent.setEngineBindings(bindings);
            for(IFunction function : functionList){
                bindings.put(function.getVarName(),function);
            }

            //注入属性变量
            buildScriptParams(bindings,apiParams);
            Object result = this.engineEval(script,bindings);
            return result;
        }catch (Exception e){
            if (e.getCause() != null && e.getCause().getCause() != null){
                throw e.getCause().getCause();
            }else{
                throw e;
            }
        }

    }

    private String buildSQLScript(String script,ApiInfo apiInfo) {

        String func = null;
        script = script.trim();
        if (selectSqlPattern.matcher(script).find()){
            if (apiInfo.getFullPath().endsWith("/page")){
                func = "pager";
            }else if(apiInfo.getFullPath().endsWith("/first")){
                func = "findOne";
            }else {
                func = "find";
            }
        }else if(insertSqlPattern.matcher(script).find()){
            func = "insert";
        }else if(deleteSqlPattern.matcher(script).find()){
            func = "remove";
        }else if(updateSqlPattern.matcher(script).find()){
            func = "update";
        }

        if (func == null){
            return script;
        }

        StringBuilder sb = new StringBuilder("db.").append(func).append("('''").append(script).append("''');");
        return sb.toString();
    }

    @Override
    public Object engineEval(String script,Bindings bindings) throws Throwable {
        try {
            return engine.eval(script,bindings);
        }catch (Exception e){
            if (e.getCause() != null && e.getCause().getCause() != null){
                throw e.getCause().getCause();
            }else{
                throw e;
            }
        }
    }



    private void buildScriptParams(Bindings bindings, ApiParams apiParams) {
        bindings.put("pathVar",apiParams.getPathVar());
        bindings.put("param",apiParams.getParam());
        bindings.put("body",apiParams.getBody());
        bindings.put("header",apiParams.getHeader());
        bindings.put("cookie",apiParams.getCookie());
        bindings.put("session",apiParams.getSession());
        bindings.put("request",apiParams.getRequest());
        bindings.put("response",apiParams.getResponse());

        if (!CollectionUtils.isEmpty(apiParams.getSession())){
            apiParams.getSession().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getCookie())){
            apiParams.getCookie().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getHeader())){
            apiParams.getHeader().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getBody())){
            apiParams.getBody().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getParam())){
            apiParams.getParam().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

        if (!CollectionUtils.isEmpty(apiParams.getPathVar())){
            apiParams.getPathVar().forEach((key,value)->{
                if (StringUtils.isEmpty(key))return;
                bindings.put(key,value);
            });
        }

    }
}

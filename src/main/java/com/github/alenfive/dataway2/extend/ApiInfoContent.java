package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:参数变量域
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 21:26
 * @UpdateDate: 2020/6/23 21:26
 * @UpdateRemark: init
 * @Version: 1.0
 */

@Component
public class ApiInfoContent {

    private ThreadLocal<ApiInfo> apiInfo = new ThreadLocal<>();
    private ThreadLocal<ApiParams> apiParams = new ThreadLocal<>();
    private ThreadLocal<List<String>> logs = new ThreadLocal<>();
    private ThreadLocal<ScriptEngine> engine = new ThreadLocal<>();

    public List<String> getLogs(){
        return logs.get();
    }
    public void putLog(String log){
        if (logs.get() == null){
            logs.set(new ArrayList<>());
        }
        logs.get().add(log);
    }

    public ApiInfo getApiInfo() {
        return apiInfo.get();
    }

    public void setApiInfo(ApiInfo apiInfo) {
        this.apiInfo.set(apiInfo);
    }

    public ApiParams getApiParams() {
        return apiParams.get();
    }

    public void setApiParams(ApiParams apiParams) {
        this.apiParams.set(apiParams);
    }

    public void setEngine(ScriptEngine engine) {
        this.engine.set(engine);
    }

    public ScriptEngine getEngine() {
        return engine.get();
    }

    public void removeAll(){
        apiInfo.remove();
        apiParams.remove();
        logs.remove();
        engine.remove();
    }
}

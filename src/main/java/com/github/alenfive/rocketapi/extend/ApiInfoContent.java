package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.List;

/**
 * 脚本执行上下文变量
 */

@Component
public class ApiInfoContent {

    private ThreadLocal<Boolean> isDebug = new InheritableThreadLocal<>();
    private ThreadLocal<ApiInfo> apiInfo = new InheritableThreadLocal<>();
    private ThreadLocal<ApiParams> apiParams = new InheritableThreadLocal<>();
    private ThreadLocal<List<String>> logs = new InheritableThreadLocal<>();
    private ThreadLocal<Bindings> engineBindings = new InheritableThreadLocal<>();

    public Boolean getIsDebug() {
        return isDebug.get() == null?false:isDebug.get();
    }

    public void setIsDebug(Boolean isDebug) {
        this.isDebug.set(isDebug);;
    }

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

    public void setEngineBindings(Bindings bindings) {
        this.engineBindings.set(bindings);
    }

    public Bindings getEngineBindings() {
        return engineBindings.get();
    }

    public void removeAll(){
        apiInfo.remove();
        apiParams.remove();
        logs.remove();
        engineBindings.remove();
        isDebug.remove();
    }
}

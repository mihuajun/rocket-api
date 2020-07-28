package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 默认全局静态函数
 */
@Component
public class UtilsFunction implements IFunction{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ScriptParseService scriptParseService;

    @Override
    public String getVarName() {
        return "Utils";
    }

    /**
     * 获取上下文中的指定变量
     * @param varName
     * @return
     */
    public Object val(String varName){
        return scriptParseService.buildParamItem(apiInfoContent.getApiParams(),varName);
    }
}

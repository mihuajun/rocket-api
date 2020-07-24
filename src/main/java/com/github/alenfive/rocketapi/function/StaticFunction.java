package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 默认全局静态函数
 */
@Component
public class StaticFunction{

    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        StaticFunction.applicationContext = applicationContext;
    }

    /**
     * 获取上下文中的指定变量
     * @param varName
     * @return
     */
    public static Object val(String varName){
        ApiInfoContent apiInfoContent = applicationContext.getBean(ApiInfoContent.class);
        return apiInfoContent.getEngine() == null?null:apiInfoContent.getEngine().getContext().getAttribute(varName);
    }
}

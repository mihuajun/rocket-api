package com.github.alenfive.rocketapi.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 异常断言函数
 */
@Component
public class ContextFunction implements IFunction{

    @Autowired
    private ApplicationContext context;

    @Override
    public String getVarName() {
        return "springContext";
    }

    public Object getBean(String beanName){
        return context.getBean(beanName);
    }
    public <T> T getBean(Class<T> beanType){
        return context.getBean(beanType);
    }
}

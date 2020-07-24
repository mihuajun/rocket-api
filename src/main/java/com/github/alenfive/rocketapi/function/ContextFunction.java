package com.github.alenfive.rocketapi.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

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
}

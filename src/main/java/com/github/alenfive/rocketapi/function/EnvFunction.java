package com.github.alenfive.rocketapi.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * spring环境变量函数
 */
@Component
public class EnvFunction implements IFunction{

    @Autowired
    private Environment environment;

    @Override
    public String getVarName() {
        return "env";
    }

    public String get(String key){
        return environment.getProperty(key);
    }

    public String get(String key,String defaultValue){
        return environment.getProperty(key,defaultValue);
    }
}

package com.github.alenfive.dataway2.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @Description: spring环境变量
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
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

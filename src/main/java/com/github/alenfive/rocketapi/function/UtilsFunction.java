package com.github.alenfive.rocketapi.function;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 常用工具类函数
 */
@Component
public class UtilsFunction implements IFunction{

    @Override
    public String getVarName() {
        return "Utils";
    }

    public boolean isNotEmpty(Object input){
        return !StringUtils.isEmpty(input);
    }

    public boolean isEmpty(Object input){
        return StringUtils.isEmpty(input);
    }

    public boolean equals(Object input1,Object input2){
        return Objects.equals(input1,input2);
    }

    public boolean regex(String rex,String input){
        if (StringUtils.isEmpty(input)){
            return false;
        }
        return input.matches(rex);
    }


}

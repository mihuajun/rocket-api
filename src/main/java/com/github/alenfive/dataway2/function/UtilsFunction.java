package com.github.alenfive.dataway2.function;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;

/**
 * @Description: 工具类函数
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
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

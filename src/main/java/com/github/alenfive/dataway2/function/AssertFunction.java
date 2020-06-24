package com.github.alenfive.dataway2.function;

import org.springframework.stereotype.Component;

/**
 * @Description:异常断言
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Component
public class AssertFunction implements IFunction{

    @Override
    public String getVarName() {
        return "Assert";
    }

    public void is(boolean validate,String ... input){
        if (!validate){
            throw new RuntimeException(input[0]);
        }
    }
}

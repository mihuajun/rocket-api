package com.github.alenfive.dataway2.extend;

import java.util.Objects;

/**
 * @Description:等值匹配
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/22 16:49
 * @UpdateDate: 2020/6/22 16:49
 * @UpdateRemark: init
 * @Version: 1.0
 */
public class EqualsValidator implements IValidator {

    @Override
    public String support() {
        return "equals";
    }

    @Override
    public Boolean validate(String... input) {

        if (input.length != 2){
            throw new RuntimeException("Parameter length mismatch" + input.toString());
        }

        return Objects.equals(input[0],input[1]);
    }
}

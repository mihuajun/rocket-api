package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/15 19:28
 * @UpdateDate: 2020/6/15 19:28
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Component
public class DefaultAssertException implements IAssertException {

    @Override
    public void exception(String... input) {
        if (input.length != 1){
            throw new RuntimeException("Parameter length mismatch" + input.toString());
        }
        throw new RuntimeException(input[0]);
    }
}

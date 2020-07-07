package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * 默认参数验证异常处理类
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

package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * 默认参数验证异常处理类
 */
@Component
public class DefaultAssertException implements IAssertException {

    @Override
    public void exception(String throwMsg,String... input) {
        throw new RuntimeException(throwMsg);
    }
}

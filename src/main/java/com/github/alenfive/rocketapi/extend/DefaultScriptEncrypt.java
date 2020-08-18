package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * 代码加解密默认实现类
 */

@Component
public class DefaultScriptEncrypt implements IScriptEncrypt {

    @Override
    public String encrypt(String source) throws Exception {
        return source;
    }

    @Override
    public String decrypt(String encrypt) throws Exception {
        return encrypt;
    }
}

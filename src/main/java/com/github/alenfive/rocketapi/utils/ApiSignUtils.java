package com.github.alenfive.rocketapi.utils;

import org.springframework.util.DigestUtils;

/**
 * API同步签名
 */
public class ApiSignUtils {
    public String sign(String key,long timestamp,String body){
        String content = "key="+key +"timestamp="+timestamp+"body="+body;
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}

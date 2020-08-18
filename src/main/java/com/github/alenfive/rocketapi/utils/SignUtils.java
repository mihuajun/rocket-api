package com.github.alenfive.rocketapi.utils;

import java.util.Map;

/**
 * API同步签名
 */
public class SignUtils {
    public static String build(String secret, Map<String, Object> params) {
        StringBuilder para_str = new StringBuilder();
        params.keySet().stream().sorted(String::compareTo).forEach(key->{
            para_str.append(key).append(params.get(key));
        });
        para_str.append(secret);
        return MD5Utils.getMD5Str(para_str.toString()).toUpperCase();
    }
}

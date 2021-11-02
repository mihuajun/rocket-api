package com.github.alenfive.rocketapi.utils;

import java.util.Map;

public class LogFormatUtils {

    public static String sqlParam(StringBuilder script, Map<String,Object>[] params){
        StringBuilder res = new StringBuilder();
        for (Map<String,Object> item : params){
            String itemStr = script.toString();
            for(String key : item.keySet()){
                itemStr = itemStr.replaceAll(":"+key,buildValue(item.get(key)));
            }
            res.append("\r\n").append(itemStr);
        }
        return res.toString();
    }

    private static String buildValue(Object value) {
        if (value == null){
            return "null";
        }
        if (value instanceof String){
            return "'"+value.toString()+"'";
        }
        return value.toString();
    }

}

package com.github.alenfive.rocketapi.utils;

import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;

@Slf4j
public class LogFormatUtils {

    public static String sqlParam(StringBuilder script, ScriptParseService parseService, ScriptContext scriptContext){
        try {
            StringBuilder res = new StringBuilder();
            for (Map<String,Object> item : scriptContext.getParams()){
                String itemStr = script.toString();
                for(String key : item.keySet()){
                    String replacement = java.util.regex.Matcher.quoteReplacement(parseService.buildFormatValue(item.get(key),scriptContext.getDataSourceDialect()));
                    itemStr = itemStr.replaceAll(":"+key,replacement);
                }
                res.append("\r\n").append(itemStr);
            }
            return res.toString();
        }catch (Exception e){
            StringBuilder res = new StringBuilder("sql param format error:").append(script);
            return res.toString();
        }
    }

    private static String buildValue(Object value) {
        if (value == null){
            return "null";
        }
        if (value instanceof String){
            return "'"+value.toString()+"'";
        }
        if (value instanceof Collection){
            Collection list = (Collection)(value);

        }
        return value.toString();
    }
}

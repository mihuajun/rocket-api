package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * api 接口入参实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParams {
    private Map<String,String> pathVar;
    private Map<String,Object> param;
    private Map<String,Object> body;
    private Map<String,String> header;
    private Map<String,Object> cookie;
    private Map<String,Object> session;
    private HttpServletRequest request;

    public ApiParams putParam(String key,Object value){
        if (param == null){
            param = new HashMap<>();
        }
        param.put(key,value);
        return this;
    }


}

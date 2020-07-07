package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * api ui 界面接口返回实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult {
    private Integer unpack = 1;
    private Integer code = 200;
    private String msg = "success";
    private Object data;

    public static ApiResult success(Object data){
        ApiResult apiResult = new ApiResult();
        apiResult.setData(data);
        return apiResult;
    }

    public static ApiResult fail(String msg){
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(-1);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static ApiResult fail(String msg,Object data){
        ApiResult apiResult = new ApiResult();
        apiResult.setCode(-1);
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }
}

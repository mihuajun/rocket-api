package com.github.alenfive.dataway2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/3 16:57
 * @UpdateDate: 2020/6/3 16:57
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 返回实体
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
}

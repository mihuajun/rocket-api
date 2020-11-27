package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiUpdateField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * API 配置对象
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiConfig {

    /**
     * 唯一标识符
     */
    private String id;

    /**
     * 配置可视化标识符
     */
    private String service;

    /**
     * 配置明细
     */
    @ApiUpdateField
    private String configContext;

    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("id",id);
        result.put("service",service);
        result.put("configContext",configContext);
        return result;
    }
}

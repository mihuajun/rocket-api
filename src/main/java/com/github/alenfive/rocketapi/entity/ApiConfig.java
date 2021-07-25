package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiTable;
import com.github.alenfive.rocketapi.annotation.ApiUpdateField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 配置对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiTable("api_config")
public class ApiConfig extends ApiEntity{

    /**
     * 配置可视化标识符
     */
    private String service;

    /**
     * 配置分类
     * @ConfigType
     */
    private String type;

    /**
     * 配置明细
     */
    @ApiUpdateField
    private String configContext;

}

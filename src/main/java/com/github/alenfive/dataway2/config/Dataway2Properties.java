package com.github.alenfive.dataway2.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/22 17:11
 * @UpdateDate: 2020/5/22 17:11
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 配置属性
 */
@ConfigurationProperties(prefix = "spring.dataway2")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dataway2Properties {
    private String basePath = "/api-ui";
}

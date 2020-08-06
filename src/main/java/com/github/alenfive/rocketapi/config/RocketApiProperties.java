package com.github.alenfive.rocketapi.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置属性
 */
@ConfigurationProperties(prefix = "spring.rocket-api")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RocketApiProperties {

    //基础注册路径
    private String baseRegisterPath = "/interface-ui";

    //密钥KEY
    private String encryptKey = "6254a1b8d0d411ea";
}

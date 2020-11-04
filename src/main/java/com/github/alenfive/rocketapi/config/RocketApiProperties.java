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
    private String secretKey = "123456789";

    //post传参 操作整个body部分的key值
    private String bodyRootKey = "bodyRoot";
}

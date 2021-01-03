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

    //数据库大小写驼峰自动转换，默认true
    private boolean mapUnderscoreToCamelase = true;

    //启用编辑管理界面，默认true,生产环境如果访问地址暴露在了外网，建议关闭
    private boolean viewEnabled = true;

    /**
     * 启用远程发布入口，默认true,生产环境如果访问地址暴露在了外网，建议关闭
     * 当为false时，不接收远程发布过来的API更新或者新增
     * 当为true时，配合secretKey使用,来达到安全远程部署的目的
     */
    private boolean syncEnabled = true;

    //启用YML配置，默认false
    private boolean configEnabled = false;

}

# 项目属性配置

1. **基础API注册路径，默认值为/interface-ui**

```
spring.rocket-api.base-register-path: /interface-ui
```

**2. 远程部署密钥，默认为：123456789**

```
spring.rocket-api.secret-key: 123456789
```

#### 3. 开启页面配置功能，实现在页面中配置springboot application.yml的能力，默认fase

```
spring.rocket-api.config-enabled: true

#开启条件
1.创建表api_config 详见数据库创建脚本，用于持久化配置信息 
2.添加以下spring cloud 依赖，用于实现springboot配置的动态更新
注意：因为配置存于数据库中，根据springboot配置顺序加载规则，所以这些配置是最晚加载的，优先级则是最高
如果需要使用 @Value(),@ConfigurationProperties 方式获取这些配置，需要开启@RefreshScope 或者bean初始于`QLRequestMappingFactory` 之后
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-context</artifactId>
</dependency>
```

#### 4.  不启用UI控制界面/interface-ui, 用于生产环境的安全性控制,默认为 true

```
spring.rocket-api.view-enabled: false
```

#### 5.  不接受远程部署，用于拒绝其他服务向本服务发布接口，默认为 true  ,对应功能 "页面操作"-> "远程发布"

```
spring.rocket-api.sync-enabled: false
```

#### 6. 驼峰自动转换配置, 默认true

```
spring.rocket-api.map-underscore-to-camel-case: true
```

{% content-ref url="../shu-ju-ku-chuang-jian-jiao-ben/" %}
[shu-ju-ku-chuang-jian-jiao-ben](../shu-ju-ku-chuang-jian-jiao-ben/)
{% endcontent-ref %}

{% content-ref url="../shu-ju-ku-chuang-jian-jiao-ben/mysql.md" %}
[mysql.md](../shu-ju-ku-chuang-jian-jiao-ben/mysql.md)
{% endcontent-ref %}





开启配置

**7. 静态资源压缩-加快页面访问速度**

```
server:
  compression:
    enabled: true
```

**8. 静态资源缓存-加快页面访问速度**

```
spring:
  resources:
    cache:
      period: 1h
```

#### 9. 表名自定义配置

```
spring:
  rocket-api:
    table-name:
      api-info: api_info
      api-config: api_config
      api-example: api_example
      api-directory: api_directory
      api-info-history: api_info_history
```

#### 10. 集群环境配置 ,默认为\`none\`单机模式，配置为\`redis\`表示以Redis来完成集群之间的信息互通，也可以自定义以其他中间件来达到目的。集群中API变更，动态数据源修改，动态配置变更，等都需要同步到集群中的所有实例，保证所有实例一致。配置方式如下:

```
spring:
  rocket-api:
    cluster-type: redis

//需要引入redis依赖，并且配置redis数据源的连接信息，确保能够使用以下对象能够正确获取
//    @Autowired
//    private StringRedisTemplate redisTemplate; 
// 然后就开心的在集群环境下玩耍吧

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

#### 11. 服务名称配置

```
spring:
  rocket-api:
    service-title: 数据开放平台     #用于UI title显示 
    service-name: DOP             #用于数据库数据隔离
```

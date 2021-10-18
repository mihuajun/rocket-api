# 数据源配置方式二(3.8.0移除)

在项目内部application.yml中配置，或者在页面rocket-api.yml中配置：

```
spring:
  rocket-api:
    multi-datasource:
      - name: mysql2
        factory-class-name: com.github.alenfive.rocketapi.datasource.factory.MySQLFactory
        config:
          jdbcUrl: jdbc:mysql://127.0.0.1:3306/mysql2
          username: root
          password: root
          driverClassName: com.mysql.cj.jdbc.Driver
      - name: oracle3
        factory-class-name: com.github.alenfive.rocketapi.datasource.factory.SQLFactory
        config:
          jdbcUrl: jdbc:oracle:thin:@localhost:1521:orcl
          username: root
          password: root
          driverClassName: oracle.jdbc.driver.OracleDriver
      - name: mongodb
        factory-class-name: com.github.alenfive.rocketapi.datasource.factory.MongoFactory
        config:
          url: mongodb://root:123@127.0.0.1:27017/test
```

#### 提示： 更多数据源自行构建factory-class-name

#### 说明：

| 字段                 | 释义                                       |
| ------------------ | ---------------------------------------- |
| multi-datasource   | 多数据源配置，值为数组结构                            |
| name               | 名称，要求唯一                                  |
| factory-class-name | 数据源构造类，可根据案例，自行扩展，以满足不同数据源要求             |
| config             | MAP结构，下面的值会传递给factory-class-name，用于构建数据源 |
| jdbcUrl            | 自行定义键值，会传递给factory-class-name解析          |
| username           | 自行定义键值，会传递给factory-class-name解析          |
| password           | 自行定义键值，会传递给factory-class-name解析          |
| driverClassName    | 自行定义键值，会传递给factory-class-name解析          |
| 其他                 | factory-class-name如果需要更多的参数，自行定义         |

#### 注意：可以配合页面动态配置功能，完成动态数据源管理

{% content-ref url="../ye-mian-cao-zuo/xiang-mu-pei-zhi-v2.3.0-gong-neng-xin-zeng.md" %}
[xiang-mu-pei-zhi-v2.3.0-gong-neng-xin-zeng.md](../ye-mian-cao-zuo/xiang-mu-pei-zhi-v2.3.0-gong-neng-xin-zeng.md)
{% endcontent-ref %}

#### factory-class-name 说明

| 关系性数据库通用   | com.github.alenfive.rocketapi.datasource.factory.SQLFactory        |
| ---------- | ------------------------------------------------------------------ |
| MySQL构造器   | com.github.alenfive.rocketapi.datasource.factory.MySQLFactory      |
| Mongodb    | com.github.alenfive.rocketapi.datasource.factory,MongoFactory      |
| ClickHouse | com.github.alenfive.rocketapi.datasource.factory.ClickHouseFactory |
| PostgreSQL | com.github.alenfive.rocketapi.datasource.factory.PostgreSQLFactory |
| SQLServer  | com.github.alenfive.rocketapi.datasource.factory.SQLServerFactory  |
| 其他数据源      | 根据以上例子自行扩展，生成spring bean 对象即可使用                                    |


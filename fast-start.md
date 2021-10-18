# 快速开始

#### 快速开始

## 1. 添加依赖 

```
<dependency>
    <groupId>com.github.alenfive</groupId>
    <artifactId>rocket-api-boot-starter</artifactId>
    <version>2.4.3.RELEASE</version>
</dependency>
```

历史版本查看: [版本发布记录](ban-ben-fa-bu-ji-lu/)

## 2. 数据源配置,继承

`com.github.alenfive.rocketapi.datasource.DataSourceManager`

```java
@Component
public class DefaultDataSourceManager extends DataSourceManager {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {

        Map<String,DataSourceDialect> dialects = new HashMap<>();
        //通过MysqlDataSource的第二个参数为`true`来表示生成的API信息所存储的库，有且仅有一个为true
        dialects.put("mysql",new MySQLDataSource(dataSource,true));
        super.setDialectMap(dialects);
    }
}
```

## 3. 关系型数据库建表，非关系型不用

最新建表脚本查看: [数据库创建脚本](shu-ju-ku-chuang-jian-jiao-ben/)

> 启动项目，访问地址: [http://localhost:8080/interface-ui](http://localhost:8080/interface-ui)

#### DEMO clone项目 快速开始

> [https://gitee.com/alenfive/rocket-api-demo](https://gitee.com/alenfive/rocket-api-demo)

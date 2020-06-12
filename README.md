# dataway2

### 快速开始
1/添加依赖
```$xml
<dependency>
    <groupId>com.github.alenfive</groupId>
    <artifactId>dataway2-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

版本查看: https://github.com/alenfive/dataway2/releases   

2/配置数据源,继承`com.github.alenfive.dataway2.extend.DataSourceManager` 注入数据源
```java
@Component
public class DefaultDataSourceManager extends DataSourceManager {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {

        Map<String,DataSourceDialect> dialects = new HashMap<>();
        dialects.put("mysql",new MysqlDataSource(jdbcTemplate,true));
        super.setDialectMap(dialects);
    }
}
```

3/启动项目，访问地址:http://localhost:8080/api-ui

![Image text](./src/main/resources/static/images/demo.png)

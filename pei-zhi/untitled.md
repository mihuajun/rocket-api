# 数据源配置方式一 （推荐）

继承抽象类`com.github.alenfive.rocketapi.datasource.DataSourceManager` 如下：\


`DataSourceDialect`的实现类中，其中成员变量:`storeApi` 有且只能有一个为`true`,表示Rocket-API运行所需数据存储所在库

```java
@Component
public class DefaultDataSourceManager extends DataSourceManager {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {
        Map<String,DataSourceDialect> dialects = new HashMap<>();
        dialects.put("mysql",new SqlDataSource(jdbcTemplate,false));
        dialects.put("mongodb",new MongoDataSource(mongoTemplate,true));
        super.setDialectMap(dialects);
    }
}
```


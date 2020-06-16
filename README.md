# dataway2

### 特性
1/支持SQL脚本查询     
2/支持mongodb command脚本查询     
3/支持单个查询        
4/支持列表查询        
5/支持分页查询        
6/支持增删改操作       
7/支持变量作用域pathVar, param, body, header, cookie，当不指定变量使用域时，按此顺序依次查找,如#{pathVar.name},#{name}      
8/支持简单的判空逻辑 #?{var,express}     
9/多数据源支持,继承自com.github.alenfive.dataway2.extend.DataSourceDialect       
10/动态注册request mapping      
11/支持自定义分页返回，默认对象：com.github.alenfive.dataway2.extend.DefaultApiPager       

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

3/关系型数据库建表，非关系型不用
```$xslt
CREATE TABLE `api_info` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `method` varchar(45) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL,
  `type` varchar(5) DEFAULT NULL COMMENT '类型：CODE,SQL',
  `service` varchar(45) DEFAULT NULL,
  `group` varchar(45) DEFAULT NULL,
  `editor` varchar(45) DEFAULT NULL,
  `comment` varchar(200) DEFAULT NULL,
  `datasource` varchar(45) DEFAULT NULL,
  `script` text,
  `params` text,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_path_method` (`path`,`method`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COMMENT='路径明细';

```

4/启动项目，访问地址:http://localhost:8080/api-ui

![Image text](./src/main/resources/static/images/demo.png)

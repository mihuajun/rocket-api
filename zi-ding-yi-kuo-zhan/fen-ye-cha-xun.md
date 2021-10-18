# 自定义分页构造器

分页涉及参数：

1. pageNo 当前页
2. pageSize 每页大小
3. index 查询起始位置，Rocket-APi自动通过pageNo,pageSize计算得来，不需要手动定义

#### 自动分页

```
db.pager("select * from user")
```

#### 手动分页

```
countSql = """
select count(1) from user
"""

querySql = """
select * from user limit #{index},#{pageSize}
"""

total = db.count(countSql);
list = db.find(querySql);
return Pager.build(total,list);
```

**自定义分页构建，实现接口com.github.alenfive.rocketapi.extend.IApiPager**

```
/**
 * 默认分页数据构建器
 */
@Component
public class DefaultApiPager implements IApiPager {

    @Autowired
    private UtilsFunction utilsFunction;

    @Override
    public Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams) {
        Map<String,Object> pager = new HashMap<>();
        Integer pageSize = Integer.valueOf(utilsFunction.val(this.getPageSizeVarName()).toString());
        Integer pageNo = Integer.valueOf(utilsFunction.val(this.getPageNoVarName()).toString());
        Integer index = Integer.valueOf(utilsFunction.val(this.getIndexVarName()).toString());

        pager.put("totalRecords",totalRecords);
        pager.put("totalPages",Integer.valueOf((int) ((totalRecords + pageSize - 1) / pageSize)));
        pager.put("data",data);
        pager.put(this.getPageNoVarName(),pageNo);
        pager.put(this.getPageSizeVarName(),pageSize);
        pager.put(this.getIndexVarName(),index);
        return pager;
    }

    @Override
    public String getPageSizeVarName() {
        return "pageSize";
    }

    @Override
    public String getPageNoVarName() {
        return "pageNo";
    }

    @Override
    public String getIndexVarName() {
        return "index";
    }

    @Override
    public Integer getIndexVarValue(Integer pageSize,Integer pageNo) {
        return (pageNo-1)*pageSize;
    }

    @Override
    public Integer getPageSizeDefaultValue() {
        return 15;
    }

    @Override
    public Integer getPageNoDefaultValue() {
        return 1;
    }
}

```

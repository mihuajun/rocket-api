# 自定义数据源



实现接口`com.github.alenfive.rocketapi.datasource.DataSourceDialect`

#### SQL默认实现:

```
/**
 * 关系型数据源，JdbcTemplate所操作的数据源
 */
public class SqlDataSource extends DataSourceDialect {

    protected JdbcTemplate jdbcTemplate;

    protected NamedParameterJdbcTemplate parameterJdbcTemplate;

    private SqlDataSource(){}

    public SqlDataSource(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.parameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public SqlDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        this.storeApi = storeApi;
        this.jdbcTemplate = jdbcTemplate;
        this.parameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }


    @Override
    public void saveApiInfo(ApiInfo apiInfo) {
        ApiJpaUtil.insert(parameterJdbcTemplate,apiInfo);
    }

    @Override
    public ApiInfo findApiInfoById(ApiInfo apiInfo) {
        return ApiJpaUtil.findById(parameterJdbcTemplate,apiInfo);
    }

    @Override
    public void deleteApiInfo(ApiInfo apiInfo) {
        ApiJpaUtil.deleteById(parameterJdbcTemplate,apiInfo);
    }

    @Override
    public void updateApiInfo(ApiInfo apiInfo) {
        ApiJpaUtil.updateById(parameterJdbcTemplate,apiInfo);
    }

    @Override
    public List<ApiInfo> listApiInfoByEntity(ApiInfo apiInfo) {
        return ApiJpaUtil.listByEntity(parameterJdbcTemplate,apiInfo);
    }

    @Override
    public void saveApiInfoHistory(ApiInfoHistory apiInfoHistory) {
        ApiJpaUtil.insert(parameterJdbcTemplate,apiInfoHistory);
    }

    @Override
    public List<ApiInfoHistory> listApiInfoHistoryByEntity(ApiInfoHistory apiInfoHistory, IApiPager apiPager, Page page) {
        return ApiJpaUtil.pageByEntity(parameterJdbcTemplate,apiInfoHistory,this,apiPager,page);
    }

    @Override
    public void saveApiExample(ApiExample apiExample) {
        ApiJpaUtil.insert(parameterJdbcTemplate,apiExample);
    }

    @Override
    public List<ApiExample> listApiExampleByEntity(ApiExample apiExample, IApiPager apiPager, Page page) {
        return ApiJpaUtil.pageByEntity(parameterJdbcTemplate,apiExample,this,apiPager,page);
    }

    @Override
    public void deleteExample(ApiExample apiExample) {
        ApiJpaUtil.deleteById(parameterJdbcTemplate,apiExample);
    }

    @Override
    public void saveApiConfig(ApiConfig apiConfig) {
        ApiJpaUtil.insert(parameterJdbcTemplate,apiConfig);
    }

    @Override
    public void updateApiConfig(ApiConfig apiConfig) {
        ApiJpaUtil.updateById(parameterJdbcTemplate,apiConfig);
    }

    @Override
    public List<ApiConfig> listApiConfigByEntity(ApiConfig apiConfig) {
        return ApiJpaUtil.listByEntity(parameterJdbcTemplate,apiConfig);
    }


    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script.toString());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement(script.toString(), Statement.RETURN_GENERATED_KEYS);
            return ps;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return keyHolder.getKeyList().stream().map(item->item.get("GENERATED_KEY")).collect(Collectors.toList());
    }

    @Override
    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager,Page page) {
        return script;
    }

    @Override
    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,  IApiPager apiPager,Page page) {
        return script;
    }

    @Override
    public String transcoding(String param) {
        return param
                .replace("\'","\\\'");
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

```

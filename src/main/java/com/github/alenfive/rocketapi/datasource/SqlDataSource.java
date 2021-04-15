package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiEntity;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.ApiJpaUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public JdbcTemplate getJdbcTemplate(){
        return this.jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(){
        return this.parameterJdbcTemplate;
    }

    @Override
    public <T extends ApiEntity> void saveEntity(T entity) {
        ApiJpaUtil.insert(parameterJdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> T findEntityById(T entity) {
        return ApiJpaUtil.findById(parameterJdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> void removeEntityById(T entity) {
        ApiJpaUtil.deleteById(parameterJdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> void updateEntityById(T entity) {
        ApiJpaUtil.updateById(parameterJdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> List<T> listByEntity(T entity) {
        return ApiJpaUtil.listByEntity(parameterJdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> List<T> pageByEntity(T entity, IApiPager apiPager, Page page) {
        return ApiJpaUtil.pageByEntity(parameterJdbcTemplate,entity,this,apiPager,page);
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

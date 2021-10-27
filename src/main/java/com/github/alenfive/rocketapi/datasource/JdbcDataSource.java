package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiEntity;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.ApiJpaUtil;
import com.github.alenfive.rocketapi.utils.DataSourceUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系型数据源，JdbcTemplate所操作的数据源
 */
public class JdbcDataSource extends DataSourceDialect implements DialectTransactionManager {

    protected DataSource dataSource;

    protected NamedParameterJdbcTemplate jdbcTemplate;

    protected PlatformTransactionManager transactionManager;

    private JdbcDataSource(){}

    public JdbcDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.transactionManager = new DataSourceTransactionManager(dataSource);
    }

    public JdbcDataSource(DataSource dataSource, boolean storeApi) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.transactionManager = new DataSourceTransactionManager(dataSource);
        this.storeApi = storeApi;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public NamedParameterJdbcTemplate getJdbcTemplate(){
        return this.jdbcTemplate;
    }


    @Override
    public <T extends ApiEntity> void saveEntity(T entity) {
        ApiJpaUtil.insert(jdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> T findEntityById(T entity) {
        return ApiJpaUtil.findById(jdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> void removeEntityById(T entity) {
        ApiJpaUtil.deleteById(jdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> void updateEntityById(T entity) {
        ApiJpaUtil.updateById(jdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> List<T> listByEntity(T entity) {
        return ApiJpaUtil.listByEntity(jdbcTemplate,entity);
    }

    @Override
    public <T extends ApiEntity> List<T> pageByEntity(T entity, IApiPager apiPager, Page page) {
        return ApiJpaUtil.pageByEntity(jdbcTemplate,entity,this,apiPager,page);
    }

    @Override
    public List<Map<String,Object>> find(ScriptContext scriptContext) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(scriptContext.getScript().toString(), scriptContext.getParams()[0]);
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public int update(ScriptContext scriptContext) {
        return jdbcTemplate.update(scriptContext.getScript().toString(), scriptContext.getParams()[0]);
    }

    @Override
    public int[] batchUpdate(ScriptContext scriptContext) throws Exception {
        return jdbcTemplate.batchUpdate(scriptContext.getScript().toString(),scriptContext.getParams());
    }

    @Override
    public int remove(ScriptContext scriptContext) {
        return jdbcTemplate.update(scriptContext.getScript().toString(), scriptContext.getParams()[0]);
    }

    @Override
    public Object insert(ScriptContext scriptContext) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(scriptContext.getScript().toString(), new MapSqlParameterSource(scriptContext.getParams()[0]), keyHolder);
        return keyHolder.getKeyList().stream().map(item->item.get("GENERATED_KEY")).collect(Collectors.toList());
    }

    @Override
    public String buildCountScript(String script, IApiPager apiPager, Page page){
        return script;
    }

    @Override
    public String buildPageScript(String script, IApiPager apiPager, Page page){
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

    @Override
    public void close() {
        DataSourceUtils.closeDataSource(dataSource);
    }
}

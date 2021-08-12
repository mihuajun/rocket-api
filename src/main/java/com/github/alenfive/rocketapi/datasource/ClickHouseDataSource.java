package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiEntity;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * mongodb 数据源操作
 */
public class ClickHouseDataSource extends JdbcDataSource {

    public ClickHouseDataSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public <T extends ApiEntity> void saveEntity(T entity) {

    }

    @Override
    public <T extends ApiEntity> T findEntityById(T entity) {
        return null;
    }

    @Override
    public <T extends ApiEntity> void removeEntityById(T entity) {

    }

    @Override
    public <T extends ApiEntity> void updateEntityById(T entity) {

    }

    @Override
    public <T extends ApiEntity> List<T> listByEntity(T entity) {
        return null;
    }

    @Override
    public <T extends ApiEntity> List<T> pageByEntity(T entity, IApiPager apiPager, Page page) {
        return null;
    }

    @Override
    public List<Map<String,Object>> find(ScriptContext scriptContext) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(scriptContext.getScript().toString(), scriptContext.getParams());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public Long update(ScriptContext scriptContext) {
        throw new UnsupportedOperationException("The operation is not allowed");
    }

    @Override
    public Long remove(ScriptContext scriptContext) {
        throw new UnsupportedOperationException("The operation is not allowed");
    }

    @Override
    public Object insert(ScriptContext scriptContext) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(scriptContext.getScript().toString(), new MapSqlParameterSource(scriptContext.getParams()), keyHolder);
        return keyHolder.getKeyList().stream().map(item->item.get("GENERATED_KEY")).collect(Collectors.toList());
    }

    @Override
    public String buildCountScript(String script,IApiPager apiPager,Page page) {
        return script;
    }

    @Override
    public String buildPageScript(String script,IApiPager apiPager,Page page) {
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

package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiExample;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiInfoHistory;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.FieldUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
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

    private SqlDataSource(){}

    public SqlDataSource(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        this.storeApi = storeApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String saveApiInfoScript() {
        return new StringBuilder("insert into api_info(")
                .append(String.join(",", FieldUtils.allFields(ApiInfo.class)))
                .append(")values(")
                .append(FieldUtils.allFields(ApiInfo.class).stream().map(item->"#{"+FieldUtils.underlineToCamel(item)+"}").collect(Collectors.joining(",")))
                .append(")")
                .toString();
    }

    @Override
    public String deleteApiInfoScript() {
        return "delete from api_info where id = #{id}";
    }

    @Override
    public String updateApiInfoScript() {
        return new StringBuilder("update api_info set ")
                .append(FieldUtils.updateFields(ApiInfo.class).stream().map(item->new StringBuilder(item).append("=").append("#{"+FieldUtils.underlineToCamel(item)+"}")).collect(Collectors.joining(",")))
                .append(" where id = #{id}")
                .toString();
    }

    @Override
    public String listApiInfoScript() {
        return new StringBuilder("select ")
                .append(String.join(",",FieldUtils.allFields(ApiInfo.class)))
                .append(" from api_info where service = #{service} ?{id, and id = #{id}}")
                .toString();
    }

    @Override
    public String saveApiInfoHistoryScript() {
        return new StringBuilder("insert into api_info_history(")
                .append(String.join(",", FieldUtils.allFields(ApiInfoHistory.class)))
                .append(") values(")
                .append(FieldUtils.allFields(ApiInfoHistory.class).stream().map(item->"#{"+FieldUtils.underlineToCamel(item)+"}").collect(Collectors.joining(",")))
                .append(")")
                .toString();
    }

    @Override
    String listApiInfoHistoryScript() {
        return new StringBuilder("select ")
                .append(String.join(",",FieldUtils.allFields(ApiInfoHistory.class)))
                .append(" from api_info_history where service = #{service} ?{apiInfoId,and api_info_id = #{apiInfoId}} order by create_time desc")
                .toString();
    }


    @Override
    public String saveApiExampleScript() {
        return new StringBuilder("insert into api_example(")
                .append(String.join(",", FieldUtils.allFields(ApiExample.class)))
                .append(")values(")
                .append(FieldUtils.allFields(ApiExample.class).stream().map(item->"#{"+FieldUtils.underlineToCamel(item)+"}").collect(Collectors.joining(",")))
                .append(")")
                .toString();
    }

    @Override
    public String deleteExampleScript() {
        return "delete from api_example where id in (#{ids})";
    }

    @Override
    public String listApiExampleScript() {
        return new StringBuilder("select ")
                .append(String.join(",", FieldUtils.allFields(ApiExample.class)))
                .append(" from api_example where api_info_id = #{apiInfoId} order by create_time desc")
                .toString();
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
        return param;
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

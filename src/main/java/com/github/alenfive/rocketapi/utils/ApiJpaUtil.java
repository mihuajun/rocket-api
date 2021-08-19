package com.github.alenfive.rocketapi.utils;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.IApiPager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@Slf4j
public class ApiJpaUtil {

    private static Map<String,String> apiJpaCache = new ConcurrentHashMap<>();

    public static void insert(NamedParameterJdbcTemplate jdbcTemplate, Object apiObject){
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());
        String key = "insert:"+tableName;
        String sql = null;
        if ((sql = apiJpaCache.get(key)) == null ){
            sql = new StringBuilder("insert into ").append(tableName)
                    .append("(")
                    .append(String.join(",", FieldUtils.allTableFields(apiObject.getClass())))
                    .append(")values(")
                    .append(String.join(",", FieldUtils.allNameParamsFields(apiObject.getClass())))
                    .append(")")
                    .toString();
        }
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        log.debug("generate script:{}",sql);
        jdbcTemplate.update(sql,parameterSource);
    }

    public static<T> List<T> listByEntity(NamedParameterJdbcTemplate jdbcTemplate, T apiObject){
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());

        String where = FieldUtils.allFields(apiObject.getClass()).stream().filter(item->{
            item.setAccessible(true);
            try {
                Object value = item.get(apiObject);
                return value != null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }).map(item->(FieldUtils.humpToLine2(item.getName())+"=:"+item.getName())).collect(Collectors.joining(" and "));

        String sql = new StringBuilder("select ")
                .append(String.join(",", FieldUtils.allTableFields(apiObject.getClass())))
                .append(" from ")
                .append(tableName)
                .append(where == null?"":" where "+where)
                .toString();

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        log.debug("generate script:{}",sql);
        return jdbcTemplate.query(sql,parameterSource,new BeanPropertyRowMapper(apiObject.getClass()));
    }

    public static<T> void deleteById(NamedParameterJdbcTemplate jdbcTemplate,T apiObject) {
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());
        String idField = ApiAnnotationUtil.getApiIdFieldName(apiObject.getClass());
        String key = "deleteById:"+tableName;
        String sql = null;
        if ((sql = apiJpaCache.get(key)) == null ){
            sql = new StringBuilder("delete from ")
                    .append(tableName)
                    .append(" where ")
                    .append(idField)
                    .append(" = :")
                    .append(idField)
                    .toString();
        }
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        log.debug("generate script:{}",sql);
        jdbcTemplate.update(sql,parameterSource);
    }

    public static<T> T findById(NamedParameterJdbcTemplate jdbcTemplate,T apiObject) {
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());
        String idField = ApiAnnotationUtil.getApiIdFieldName(apiObject.getClass());
        String key = "findById:"+tableName;
        String sql = null;
        if ((sql = apiJpaCache.get(key)) == null ){
            sql = new StringBuilder("select ")
                    .append(String.join(",", FieldUtils.allTableFields(apiObject.getClass())))
                    .append(" from ")
                    .append(tableName)
                    .append(" where ")
                    .append(idField)
                    .append(" = :")
                    .append(idField)
                    .toString();
        }
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        log.debug("generate script:{}",sql);
        List<T> apiInfos = jdbcTemplate.query(sql,parameterSource,new BeanPropertyRowMapper(apiObject.getClass()));
        if (CollectionUtils.isEmpty(apiInfos)){
            return null;
        }
        return apiInfos.get(0);
    }

    public static<T> void updateById(NamedParameterJdbcTemplate jdbcTemplate,T apiObject) {
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());
        String idField = ApiAnnotationUtil.getApiIdFieldName(apiObject.getClass());
        String key = "updateById:"+tableName;
        String sql = null;
        if ((sql = apiJpaCache.get(key)) == null ){
            sql = new StringBuilder("update ")
                    .append(tableName)
                    .append(" set ")
                    .append(String.join(",", FieldUtils.allTableFields(apiObject.getClass()).stream().filter(item->!item.equals(idField)).map(item->(item+"=:"+FieldUtils.underlineToCamel(item))).collect(Collectors.toList())))
                    .append(" where ")
                    .append(idField)
                    .append(" = :")
                    .append(idField)
                    .toString();
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        log.debug("generate script:{}",sql);
        jdbcTemplate.update(sql,parameterSource);
    }


    public static<T> List<T> pageByEntity(NamedParameterJdbcTemplate jdbcTemplate, T apiObject, DataSourceDialect sqlDataSource, IApiPager apiPager, Page page) {
        String tableName = ApiAnnotationUtil.getApiTableName(apiObject.getClass());
        String where = FieldUtils.allFields(apiObject.getClass()).stream().filter(item->{
            item.setAccessible(true);
            try {
                Object value = item.get(apiObject);
                return value != null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }).map(item->(FieldUtils.humpToLine2(item.getName())+"=:"+item.getName())).collect(Collectors.joining(" and "));

        String sql = new StringBuilder("select ")
                .append(String.join(",", FieldUtils.allTableFields(apiObject.getClass())))
                .append(" from ")
                .append(tableName)
                .append(where == null?"":" where "+where)
                .append(" order by id desc ")
                .toString();
        sql = sqlDataSource.buildPageScript(sql,apiPager,page);
        log.debug("generate script:{}",sql);
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(apiObject);
        return jdbcTemplate.query(sql,parameterSource,new BeanPropertyRowMapper(apiObject.getClass()));
    }
}

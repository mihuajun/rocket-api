package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.vo.FieldInfo;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.SqlUtils;

import javax.sql.DataSource;
import java.util.*;

/**
 * mysql 数据源
 */
public class MySQLDataSource extends JdbcDataSource {


    public MySQLDataSource(DataSource dataSource) {
        super(dataSource);
    }

    public MySQLDataSource(DataSource dataSource, boolean storeApi) {
        super(dataSource, storeApi);
    }

    @Override
    public String buildCountScript(String script, IApiPager apiPager, Page page) {
        return  "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, IApiPager apiPager, Page page) {
        Integer offset = apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo());
        return  script + " limit "+offset+","+page.getPageSize();
    }

    @Override
    public String transcoding(String param) {
        return param
                .replace("\\","\\\\")
                .replace("\"","\\\"")
                .replace("\'","\\\'");
    }

    @Override
    public List<TableInfo> buildTableInfo(){
        try {
            List<TableInfo> tableInfos = new ArrayList<>();
            List<Map<String,Object>> tables = jdbcTemplate.queryForList("show tables",Collections.EMPTY_MAP);
            for (Map<String,Object> table : tables){
                Set<String> keys = table.keySet();
                String tableName = table.get(keys.toArray(new String[]{})[0]).toString();
                Map<String,Object> fields = jdbcTemplate.queryForMap("show create table "+tableName,Collections.EMPTY_MAP);

                //只处理逻辑表
                if (fields.get("Create Table") == null){
                    continue;
                }
                String tableInfo = fields.get("Create Table").toString();
                String tableComment = SqlUtils.getByPattern(tableInfo, "\\) .* COMMENT='(.*)'", 1);
                List<FieldInfo> fieldInfos = new ArrayList<>();
                tableInfos.add(TableInfo.builder()
                        .name(tableName)
                        .comment(tableComment)
                        .fields(fieldInfos)
                        .build());
                List<String> fieldStrList = SqlUtils.getColumnSqls(tableInfo);
                for (String oneLine : fieldStrList) {
                    String fieldName = SqlUtils.getByPattern(oneLine, "`(.*)`", 1);
                    String fieldComment = SqlUtils.getByPattern(oneLine, "COMMENT '(.*)'", 1);
                    String fieldType = SqlUtils.getByPattern(oneLine, "`" + fieldName + "` ([A-Za-z]*)", 1);
                    fieldInfos.add(FieldInfo.builder()
                            .name(fieldName)
                            .comment(fieldComment)
                            .type(fieldType)
                            .build());
                }
            }
            return tableInfos;
        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();

    }
}

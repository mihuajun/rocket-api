package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * oracle 数据源
 */
public class OracleDataSource extends SqlDataSource {


    public OracleDataSource(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public OracleDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        super(jdbcTemplate, storeApi);
    }

    @Override
    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        return "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        Integer offset = apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo());
        Integer endIndex = offset + page.getPageSize();
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
                script + " ) TMP WHERE ROWNUM <= "+endIndex+" ) WHERE ROW_ID > "+offset;
    }

    @Override
    public String transcoding(String param) {
        // 单引号是oracle字符串的边界,oralce中用2个单引号代表1个单引号
        String afterDecode = param.replaceAll("'", "''");
        // 由于使用了/作为ESCAPE的转义特殊字符,所以需要对该字符进行转义
        // 使用转义字符 /,对oracle特殊字符% 进行转义,只作为普通查询字符，不是模糊匹配
        afterDecode = afterDecode.replaceAll("%", "/%");
        // 使用转义字符 /,对oracle特殊字符_ 进行转义,只作为普通查询字符，不是模糊匹配
        afterDecode = afterDecode.replaceAll("_", "/_");
        return afterDecode;
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;

import javax.sql.DataSource;
import java.util.List;

/**
 * oracle 数据源
 */
public class OracleDataSource extends JdbcDataSource {


    public OracleDataSource(DataSource dataSource) {
        super(dataSource);
    }

    public OracleDataSource(DataSource dataSource, boolean storeApi) {
        super(dataSource, storeApi);
    }

    @Override
    public String buildCountScript(String script, IApiPager apiPager, Page page) {
        return "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script,IApiPager apiPager, Page page) {
        Integer offset = apiPager.getOffset(page.getPageSize(),page.getPageNo());
        Integer endIndex = offset + page.getPageSize();
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
                script + " ) TMP WHERE ROWNUM <= "+endIndex+" ) WHERE ROW_ID > "+offset;
    }

    @Override
    public String transcoding(String param) {
        return param
                .replace("'", "''");
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * sql server 分页构造
 */
@Component
public class PagerDB2Dialect implements IPagerDialect {

    @Override
    public String match() {
        return ":db2:";
    }

    @Override
    public String buildCountScript(String script, Integer index, Integer pageSize) {
        return "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, Integer offset, Integer limit) {
        return "SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( " + script +
                " ) AS TMP_PAGE) TMP_PAGE WHERE ROW_ID BETWEEN "+(offset+1)+" AND "+(offset + limit);
    }
}

package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * oracle 分页构造
 */
@Component
public class OraclePagerDialect implements IPagerDialect {

    @Override
    public String match() {
        return ":oracle:";
    }

    @Override
    public String buildCountScript(String script, Integer index, Integer pageSize) {
        return "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, Integer offset, Integer limit) {
        limit = (offset >= 1) ? (offset + limit) : limit;
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " +
                script + " ) TMP WHERE ROWNUM <= "+limit+" ) WHERE ROW_ID > "+offset;
    }
}

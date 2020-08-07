package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

/**
 * mysql 分页构造
 */
@Component
public class PagerMySQLDialect implements IPagerDialect {

    @Override
    public String match() {
        return ":mysql:";
    }

    @Override
    public String buildCountScript(String script, Integer offset, Integer limit) {
        return  "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, Integer offset, Integer limit) {
        return  script + " limit "+offset+","+limit;
    }

}

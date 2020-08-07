package com.github.alenfive.rocketapi.extend;

/**
 * 分页方言
 */
public interface IPagerDialect {

    /**
     * JDBC url 匹配项
     */
    String match();

    /**
     * 分页统计脚本生成
     */
    String buildCountScript(String script, Integer offset,Integer limit);

    /**
     * 分页查询脚本生成
     */
    String buildPageScript(String script, Integer offset,Integer limit);

}

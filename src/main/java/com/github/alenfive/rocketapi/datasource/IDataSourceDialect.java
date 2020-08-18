package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.vo.TableInfo;

import java.util.List;

/**
 * 分页方言
 */
public interface IDataSourceDialect {

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

    /**
     * 表结构信息获取
     * @return
     */
    public List<TableInfo> buildTableInfo();
}

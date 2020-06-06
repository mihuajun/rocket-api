package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiDataSource;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 6/6/2020 4:26 PM
 * @UpdateDate: 6/6/2020 4:26 PM
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 数据源组
 */
public interface DataSourceManagerInterface {
    List<ApiDataSource> listDataSourceGroup();
}

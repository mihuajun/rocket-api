package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 6/6/2020 4:28 PM
 * @UpdateDate: 6/6/2020 4:28 PM
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 默认数据源管理器
 */
@Component
public class DefaultDataSourceManager implements DataSourceManagerInterface{

    @Autowired
    private DataSource dataSource;

    @Override
    public List<ApiDataSource> listDataSourceGroup() {
        List<ApiDataSource> result = new ArrayList<>(1);
        result.add(ApiDataSource.builder()
                .id("Default")
                .storeApi(true)
                .dataSource(dataSource)
                .build());

        result.add(ApiDataSource.builder()
                .id("统计")
                .storeApi(false)
                .dataSource(dataSource)
                .build());
        return result;
    }
}

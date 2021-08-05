package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.ClickHouseDataSource;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.entity.DBConfig;
import org.springframework.stereotype.Component;

/**
 * SQL  构造器
 */
@Component
public class ClickHouseDriver extends JdbcDriver {

    @Override
    public String getName() {
        return "ClickHouse";
    }

    @Override
    public String getIcon() {
        return "rocketapi/images/ClickHouse.png";
    }

    @Override
    public String getFormat() {
        return "jdbc:clickhouse://localhost:8123";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) throws Exception {
        return new ClickHouseDataSource(super.getDataSource(config));
    }
}

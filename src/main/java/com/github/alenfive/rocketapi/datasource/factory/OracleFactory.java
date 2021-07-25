package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.OracleDataSource;
import com.github.alenfive.rocketapi.entity.DBConfig;

public class OracleFactory extends JdbcFactory{
    @Override
    String getName() {
        return "oracle";
    }

    @Override
    String getIcon() {
        return "rocketapi/image/oracle.png";
    }

    @Override
    String getFormat() {
        return "jdbc:oracle:thin:@localhost:1521/test";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) throws Exception {
        return new OracleDataSource(super.getJdbcTemplate(config));
    }
}

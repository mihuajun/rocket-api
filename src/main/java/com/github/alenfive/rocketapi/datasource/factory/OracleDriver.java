package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.OracleDataSource;
import com.github.alenfive.rocketapi.entity.DBConfig;
import org.springframework.stereotype.Component;

@Component
public class OracleDriver extends JdbcDriver {

    @Override
    public String getName() {
        return "Oracle";
    }

    @Override
    public String getIcon() {
        return "rocketapi/images/Oracle.png";
    }

    @Override
    public String getFormat() {
        return "jdbc:oracle:thin:@localhost:1521/test";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) throws Exception {
        return new OracleDataSource(super.getDataSource(config));
    }
}

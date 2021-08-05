package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.MySQLDataSource;
import com.github.alenfive.rocketapi.entity.DBConfig;
import org.springframework.stereotype.Component;

/**
 * SQL  构造器
 */
@Component
public class MySQLDriver extends JdbcDriver {

    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    public String getIcon() {
        return "rocketapi/images/MySQL.png";
    }

    @Override
    public String getFormat() {
        return "jdbc:mysql://localhost:3306/test";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) throws Exception {
        return new MySQLDataSource(super.getDataSource(config));
    }
}

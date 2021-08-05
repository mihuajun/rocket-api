package com.github.alenfive.rocketapi.datasource.factory;


import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.SQLServerDataSource;
import com.github.alenfive.rocketapi.entity.DBConfig;
import org.springframework.stereotype.Component;

/**
 * SQL  构造器
 */
@Component
public class SQLServerDriver extends JdbcDriver {

    @Override
    public String getName() {
        return "Microsoft SQL Server";
    }

    @Override
    public String getIcon() {
        return "rocketapi/images/SQLServer.png";
    }

    @Override
    public String getFormat() {
        return "jdbc:sqlserver://localhost:1433;database=test";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) throws Exception {
        return new SQLServerDataSource(super.getDataSource(config));
    }
}

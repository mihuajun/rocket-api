package com.github.alenfive.rocketapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.factory.IDataSourceDialectDriver;
import com.github.alenfive.rocketapi.entity.ApiConfig;
import com.github.alenfive.rocketapi.entity.ConfigType;
import com.github.alenfive.rocketapi.entity.DBConfig;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
public class DataSourceService {

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigService configService;

    public List<ApiConfig> getDBConfig(){
        return dataSourceManager.getStoreApiDataSource().listByEntity(ApiConfig.builder().service(rocketApiProperties.getServiceName()).type(ConfigType.DB.name()).build());
    }

    @Transactional
    public void deleteDBConfig(DBConfig dbConfig) throws IOException {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setId(dbConfig.getId());
        apiConfig = configService.getConfigById(apiConfig);

        dataSourceManager.getStoreApiDataSource().removeEntityById(apiConfig);

        dbConfig = objectMapper.readValue(apiConfig.getConfigContext(),DBConfig.class);
        this.closeDBConfig(dbConfig);
    }

    private void assertDBConfigName(String dbName,String dbId) throws IOException {
        List<ApiConfig> dbList = getDBConfig();
        for (ApiConfig apiConfig : dbList){
            DBConfig dbConfig = objectMapper.readValue(apiConfig.getConfigContext(),DBConfig.class);
            if (dbConfig.getName().equals(dbName) && !dbConfig.getId().equals(dbId)){
                throw new IllegalArgumentException("name:"+dbName+" already exist");
            }
        }
    }

    /**
     * 保存数据源
     * @param dbConfig
     */
    @Transactional
    public String saveDBConfig(DBConfig dbConfig) throws Exception {
        ApiConfig apiConfig = ApiConfig.builder()
                .service(rocketApiProperties.getServiceName())
                .type(ConfigType.DB.name())
                .build();

        assertDBConfigName(dbConfig.getName(),dbConfig.getId());

        if (StringUtils.isEmpty(dbConfig.getId())) {
            dbConfig.setId(GenerateId.get().toHexString());
            apiConfig.setId(dbConfig.getId());
            apiConfig.setConfigContext(objectMapper.writeValueAsString(dbConfig));
            dataSourceManager.getStoreApiDataSource().saveEntity(apiConfig);
        } else {
            apiConfig.setId(dbConfig.getId());
            apiConfig.setConfigContext(objectMapper.writeValueAsString(dbConfig));
            dataSourceManager.getStoreApiDataSource().updateEntityById(apiConfig);
        }

        reLoadDBConfig(dbConfig);
        return dbConfig.getId();
    }

    public void testDBConfig(DBConfig config) throws Exception {
        IDataSourceDialectDriver factory = (IDataSourceDialectDriver)(Class.forName(config.getDriver()).newInstance());
        DataSourceDialect dialect = factory.factory(config);
        dialect.close();
    }

    private void closeDBConfig(DBConfig config){
        DataSourceDialect dataSourceDialect = dataSourceManager.getDialectMap().remove(config.getName());
        if (dataSourceDialect == null){
            return;
        }
        dataSourceDialect.close();
    }

    /**
     * 动态数据源加载
     * @param config
     */
    private void reLoadDBConfig(DBConfig config) throws Exception {
        DataSourceDialect history = dataSourceManager.getDialectMap().remove(config.getName());
        if (history != null){
            history.close();
        }

        if (!config.isEnabled()){
            return;
        }

        IDataSourceDialectDriver factory = (IDataSourceDialectDriver)(Class.forName(config.getDriver()).newInstance());
        DataSourceDialect dialect = factory.factory(config);
        dialect.setDynamic(true);

        dataSourceManager.getDialectMap().put(config.getName(),dialect);
    }

}

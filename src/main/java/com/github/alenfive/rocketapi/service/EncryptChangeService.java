package com.github.alenfive.rocketapi.service;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.UnaryOperator;

/**
 * 密钥修改，只对APIINFO信息有效，对于历史记录解释时可能存在异常
 */
@Service
public class EncryptChangeService {

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ScriptParseService parseService;

    public void convert(UnaryOperator<String> predicate) throws Exception {
        for (ApiInfo apiInfo : mappingFactory.getPathList(false) ){
            apiInfo.setScript(predicate.apply(apiInfo.getScript()));
            ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceManager.updateApiInfoScript());
            dataSourceManager.update(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),apiParams);
        }
    }
}

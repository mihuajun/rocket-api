package com.github.alenfive.rocketapi.service;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.ApiInfo;
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
    private ApiInfoService apiInfoService;

    public void convert(UnaryOperator<String> predicate) throws Exception {
        for (ApiInfo apiInfo : apiInfoService.getPathList(false) ){
            apiInfo.setScript(predicate.apply(apiInfo.getScript()));
            dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);
        }
    }
}

package com.github.alenfive.rocketapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.entity.ApiResult;
import com.github.alenfive.rocketapi.entity.vo.AcceptApiInfoSyncReq;
import com.github.alenfive.rocketapi.service.ApiInfoService;
import com.github.alenfive.rocketapi.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程操作
 */
@Slf4j
@RestController
@RequestMapping("${spring.rocket-api.base-register-path:/interface-ui}")
public class RemoteController {

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Autowired
    private ApiInfoService apiInfoService;

    /**
     * 接收远程同步过来的API INFO信息
     * @param syncReq
     */
    @PostMapping("/accept-sync")
    public ApiResult apiInfoSync(@RequestBody AcceptApiInfoSyncReq syncReq) throws Exception {
        if (syncReq == null
                || StringUtils.isEmpty(syncReq.getSign())
                || syncReq.getApiInfos() == null
                || syncReq.getTimestamp() == null
                || syncReq.getIncrement() == null){
            return ApiResult.fail("Parameter is missing");
        }

        if (!rocketApiProperties.isSyncEnabled()){
            return ApiResult.fail("Accept Sync already Disabled");
        }

        //签名验证
        Map<String,Object> signMap = new HashMap<>();
        signMap.put("timestamp",syncReq.getTimestamp());
        signMap.put("increment",syncReq.getIncrement());
        signMap.put("apiInfos",objectMapper.writeValueAsString(syncReq.getApiInfos()));
        signMap.put("directories",objectMapper.writeValueAsString(syncReq.getDirectories()));
        String sign = SignUtils.build(rocketApiProperties.getSecretKey(),signMap);
        if (!syncReq.getSign().equals(sign)){
            return ApiResult.fail("Signature abnormal");
        }
        try {
            Object result = apiInfoService.apiInfoSync(syncReq.getDirectories(),syncReq.getApiInfos(),syncReq.getIncrement() == 1);

            //刷新缓存
            apiInfoService.reLoadApiInfo(false);

            return ApiResult.success(result);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }
}

package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.vo.RefreshMapping;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API信息缓存
 */
@Component
public class DefaultApiInfoCache implements IApiInfoCache {

    private Map<String, ApiInfo> cacheApiInfo = new ConcurrentHashMap<>();

    private String instanceId = GenerateId.get().toHexString();

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Override
    public ApiInfo get(ApiInfo apiInfo){
        return cacheApiInfo.get(buildApiInfoKey(apiInfo));
    }

    @Override
    public Collection<ApiInfo> getAll() {
        return cacheApiInfo.values();
    }

    @Override
    public void removeAll() {
        cacheApiInfo.clear();
    }

    @Override
    public void remove(ApiInfo apiInfo) {
        cacheApiInfo.remove(buildApiInfoKey(apiInfo));
    }

    @Override
    public void put(ApiInfo apiInfo) {
        cacheApiInfo.put(buildApiInfoKey(apiInfo),apiInfo);
    }

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return apiInfo.getMethod() +" "+ apiInfo.getFullPath();
    }

    /**
     * 发送系统缓存刷新的通知
     * 1. 在页面触发"Rebuild API List"操作时，会触发此方法,refreshMapping为空，可使用Redis消息通知功能重写该方法，
     * 2. 在页面触发接口编辑"Save"操作时，会触发此方法,refreshMapping为变更记录，可使用Redis消息通知功能重写该方法，
     * 以达到分布式环境下多实例部署系统更新问题
     */
    @Override
    public void refreshNotify(RefreshMapping refreshMapping) {
        this.receiveNotify(instanceId,refreshMapping);
    }

    /**
     * 监听 "@refreshNotify"行为，来重载本地request mapping等本地实体行为的重新初始化
     * @param instanceId
     */
    @Override
    public void receiveNotify(String instanceId, RefreshMapping refreshMapping) {
        //避免本实例重复初始化
        if (this.instanceId.equals(instanceId)){
            return;
        }

        //刷新单个接口
        if (refreshMapping != null){
            try {
                mappingFactory.refreshMapping(refreshMapping);
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }

        //全局刷新
        try {
            mappingFactory.buildInit();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

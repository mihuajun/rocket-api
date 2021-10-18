# 自定义API缓存

**当多实例分布式部署时，在页面中编辑脚本后，如何把变更同步到所有实例上，这时可以自定义缓存，通过共公组件如Redis等来维护缓存问题，即所有实例共享一份缓存，跳过变更同步问题。**

IApiInfoCache.refreshNotify,receiveNotify，可实现集群环境下不重启实例也能达到各实例缓存的自动刷新和mapping重载问题。基于spring cloud 配置中心刷新思想.触发方式为：\
 1\. 通过页面上的"Rebuild API List" 行为触发\
 2\. 对任一接口进行编辑，新增，删除



**如果搞不清楚，那就重启所有实例，没有什么问题是重启不能解决的**

**实现接口:com.github.alenfive.rocketapi.extend.IApiInfoCache**

**默认实现：**

```
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
```

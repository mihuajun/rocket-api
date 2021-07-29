package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.vo.NotifyEntity;
import com.github.alenfive.rocketapi.entity.vo.NotifyEventType;
import com.github.alenfive.rocketapi.service.DataSourceService;
import com.github.alenfive.rocketapi.service.RequestMappingService;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 集群通知
 */
@Component
public class DefaultClusterNotify implements IClusterNotify {


    private String instanceId = GenerateId.get().toHexString();

    @Autowired
    private RequestMappingService requestMappingService;

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 发送系统缓存刷新的通知
     * 1. 在页面触发"Rebuild API List"操作时，会触发此方法
     * 2. 在页面触发接口编辑"Save"操作时，会触发此方法
     * 3. 动态数据源变更时，会触发此方法
     * 以达到分布式环境下多实例部署系统更新问题
     */
    @Override
    public void sendNotify(NotifyEntity notifyEntity) {
        this.receiveNotify(instanceId,notifyEntity);
    }

    /**
     * 监听 "@refreshNotify"行为，来重载本地request mapping等本地实体行为的重新初始化
     * @param instanceId
     */
    @Override
    public void receiveNotify(String instanceId, NotifyEntity notifyEntity) {
        //避免本实例重复初始化
        if (this.instanceId.equals(instanceId)){
            return;
        }

        //重新初始化
        if (NotifyEventType.ReInit.equals(notifyEntity.getEventType())){
            try {
                mappingFactory.reInit(false);
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }

        //刷新单个接口
        if (NotifyEventType.RefreshMapping.equals(notifyEntity.getEventType())){
            try {
                requestMappingService.refreshMapping(notifyEntity.getRefreshMapping());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        //刷新数据源
        if (NotifyEventType.RefreshDB.equals(notifyEntity.getEventType())){
            try {
                dataSourceService.refreshDB(notifyEntity.getRefreshDB());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

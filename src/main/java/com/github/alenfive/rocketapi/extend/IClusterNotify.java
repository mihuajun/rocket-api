package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.vo.NotifyEntity;

/**
 * API信息缓存
 */
public interface IClusterNotify {
    public void sendNotify(NotifyEntity notifyEntity);
    public void receiveNotify(NotifyEntity notifyEntity);
}

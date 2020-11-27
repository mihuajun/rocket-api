package com.github.alenfive.rocketapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;

/**
 * 配置bean 刷新
 */

public class RefreshApiConfig {

    @Autowired
    private ContextRefresher contextRefresher;

    public void refresh(){
        contextRefresher.refresh();
    }
}

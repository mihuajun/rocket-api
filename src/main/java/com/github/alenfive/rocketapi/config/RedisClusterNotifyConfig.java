package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.extend.IClusterNotify;
import com.github.alenfive.rocketapi.extend.RedisClusterNotify;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@ConditionalOnProperty(value = "spring.rocket-api.cluster-type",havingValue = "redis")
public class RedisClusterNotifyConfig {

    @Bean
    public IClusterNotify getClusterNotify(){
        return new RedisClusterNotify();
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter,
                                                   RedisClusterNotify clusterNotify) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        String channelName = clusterNotify.buildChannelName();
        container.addMessageListener(listenerAdapter, new PatternTopic(channelName));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(RedisClusterNotify clusterNotify) {
        return new MessageListenerAdapter(clusterNotify, "onMessage");
    }


}
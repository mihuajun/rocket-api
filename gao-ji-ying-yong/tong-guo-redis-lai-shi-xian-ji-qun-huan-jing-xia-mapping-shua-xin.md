---
description: 2.3.7.RELEASE新增功能
---

# 通过redis来实现集群环境下mapping刷新(2.4.0移除)

#### 业务描述

在分布式的场景中，应用多实例部署是一个常态。 Rocket-API通过动态注册springboot RequestMapping来实现API的动态管理和注册，那么在多实例的情况下，如何实现在一个实例中编辑某个接口后把变更同步到其他实例，是我们需要迫切解决的问题，（虽然我们的保底方案是重启所有实例，那也丧失了动态管理API的能力），Rocket-API提供了接口变更的回调接口，当发生接口的新增，修改，删除时，将会触发\`IApiInfoCache.refreshNotify\`方法，开发者可以实现此接口来实现集群内的广播，下面的例子使用了Redis的消息订阅方式，也可以使用其他方法，开发者自行扩展

#### 1. 重写\`com.github.alenfive.rocketapi.extend.IApiInfoCache\`抽象类

```
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.vo.RefreshMapping;
import com.github.alenfive.rocketapi.extend.IApiInfoCache;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomApiInfoCache implements IApiInfoCache ,MessageListener{

    @Value("${spring.application.name}")
    private String service;

    @Autowired
    @Lazy
    private QLRequestMappingFactory mappingFactory;

    private String instanceId = GenerateId.get().toHexString();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String buildPrefix(){
        return "rocket-api:"+service;
    }

    public String buildChannelName(){
        return "rocket-api:"+service+":channel";
    }

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return buildPrefix()+":"+apiInfo.getMethod() +"-"+ apiInfo.getFullPath();
    }

    @Override
    public ApiInfo get(ApiInfo apiInfo) {
        String strValue = redisTemplate.opsForValue().get(buildApiInfoKey(apiInfo));
        try {
            return objectMapper.readValue(strValue,ApiInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void put(ApiInfo apiInfo) {
        try {
            String strValue = objectMapper.writeValueAsString(apiInfo);
            redisTemplate.opsForValue().set(buildApiInfoKey(apiInfo),strValue);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(ApiInfo apiInfo) {
        redisTemplate.delete(buildApiInfoKey(apiInfo));
    }

    @Override
    public void removeAll() {
        redisTemplate.delete(getKeys());
    }

    private List<String> getKeys(){
        String patternKey = buildPrefix()+":*";
        ScanOptions options = ScanOptions.scanOptions()
                .count(10000)
                .match(patternKey).build();
        RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
        List<String> keys = new ArrayList<>();
        while(cursor.hasNext()){
            keys.add(cursor.next().toString());
        }
        try {
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keys;
    }

    @Override
    public Collection<ApiInfo> getAll() {
        return redisTemplate.opsForValue().multiGet(getKeys()).stream().map(item->{
            try {
                return objectMapper.readValue(item,ApiInfo.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    @Override
    public void refreshNotify(RefreshMapping refreshMapping) {
        NotifyEntity notifyEntity = new NotifyEntity();
        notifyEntity.setRefreshMapping(refreshMapping);
        notifyEntity.setIdentity(this.instanceId);
        String messageStr = null;
        try {
            messageStr = objectMapper.writeValueAsString(notifyEntity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        redisTemplate.convertAndSend(buildChannelName(), messageStr);
    }

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

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String messageStr = new String(message.getBody());
        NotifyEntity notifyEntity = null;
        try {
            notifyEntity = objectMapper.readValue(messageStr, NotifyEntity.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        this.receiveNotify(notifyEntity.getIdentity(),notifyEntity.getRefreshMapping());
    }

}
```

#### 2. 监听Redis消息

```
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisListenerConfig {

    @Autowired
    private CustomApiInfoCache apiInfoCache;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        String channelName = apiInfoCache.buildChannelName();
        container.addMessageListener(listenerAdapter, new PatternTopic(channelName));
        return container;
    }

    @Bean
    public MessageListenerAdapter listenerAdapter(CustomApiInfoCache receiver) {
        return new MessageListenerAdapter(receiver, "onMessage");
    }
}
```

#### 3. 自定义消息通过实体

```
import com.github.alenfive.rocketapi.entity.vo.RefreshMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyEntity {
    private RefreshMapping refreshMapping;
    private String identity;
}

```

#### 4. 结果如下

1. **对某一接口进行新增，修改，删除时，会触发集群环境下的mapping更新**
2. **在执行全局"Rebuild API List" 或 "Remote Release" 时会触发集群环境下所有资源的重新加载**

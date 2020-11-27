package com.github.alenfive.rocketapi.extend;

import java.lang.reflect.ParameterizedType;

/**
 * 配置变更监听
 */
public abstract class IConfigListener<T>{
    public Class getTClass(){
        Object clazz = ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (!(clazz instanceof ParameterizedType)){
            return (Class)clazz;
        }
        return (Class) ((ParameterizedType)clazz).getRawType();
    }
    public abstract void execute(T property) throws Exception;
}

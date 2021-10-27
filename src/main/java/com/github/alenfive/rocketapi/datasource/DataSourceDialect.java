package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.config.SpringContextUtils;
import com.github.alenfive.rocketapi.entity.ApiEntity;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.FieldUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源方言抽象类
 */
public abstract class DataSourceDialect {

    private boolean isDynamic = false;

    protected boolean storeApi = false;

    public boolean isStoreApi() {
        return storeApi;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    public abstract <T extends ApiEntity> void saveEntity( T entity);
    public abstract <T extends ApiEntity> T findEntityById( T entity);
    public abstract <T extends ApiEntity> void removeEntityById(T entity);
    public abstract <T extends ApiEntity> void updateEntityById(T entity);
    public abstract <T extends ApiEntity> List<T> listByEntity(T entity);
    public abstract <T extends ApiEntity> List<T> pageByEntity(T entity, IApiPager apiPager, Page page);

    //查询对象
    public abstract List<Map<String,Object>> find(ScriptContext scriptContext) throws Exception;

    //返回影响的行数
    public abstract int update(ScriptContext scriptContext) throws Exception;

    //返回执行状态
    public abstract int[] batchUpdate(ScriptContext scriptContext) throws Exception;

    //返回影响的行数
    public abstract int remove(ScriptContext scriptContext) throws Exception;

    //返回主键
    public abstract Object insert(ScriptContext scriptContext) throws Exception;

    /**
     * 替换key
     */
    protected Map<String,Object> toReplaceKeyLow(Map<String,Object> map){
        RocketApiProperties properties = SpringContextUtils.getApplicationContext().getBean(RocketApiProperties.class);
        if (!properties.isMapUnderscoreToCamelCase()){
            return map;
        }
        Map<String,Object> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            result.put(FieldUtils.underlineToCamel(key),map.get(key));
        }
        return result;
    }

    public abstract String buildCountScript(String script, IApiPager apiPager, Page page);

    public abstract String buildPageScript(String script, IApiPager apiPager, Page page);

    //入参转码
    public abstract String transcoding(String param);
    public abstract List<TableInfo> buildTableInfo();
    public abstract void close();
}

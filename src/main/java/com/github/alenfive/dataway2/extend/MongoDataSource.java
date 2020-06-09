package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/27 17:19
 * @UpdateDate: 2020/5/27 17:19
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu mongo数据源
 */
@Transactional
public class MongoDataSource extends DataSourceDialect {

    private MongoTemplate mongoTemplate;

    public MongoDataSource(MongoTemplate mongoTemplate,boolean storeApi) {
        this.mongoTemplate = mongoTemplate;
        this.storeApi = storeApi;
    }

    @Override
    public String listApiInfoScript() {
        return "{\n" +
                "\t\"find\":\"api_info\",\n" +
                "\t\"filter\":{\n" +
                "\t\t\"service\":#{service}\n" +
                "\t}\n" +
                "}";
    }

    @Override
    public String getApiInfoScript() {
        return "{\n" +
                "     find: \"api_info\",\n" +
                "     filter: { method: #{method}, path: #{path} },\n" +
                "     limit: 1\n" +
                "}";
    }

    @Override
    public String saveApiInfoScript() {
        return "{\n" +
                "\t\"insert\":\"api_info\",\n" +
                "\t\"documents\":[{\n" +
                "\t\t\"method\":#{method},\n" +
                "\t\t\"path\":#{path},\n" +
                "\t\t\"type\":#{type},\n" +
                "\t\t\"service\":#{service},\n" +
                "\t\t\"group\":#{group},\n" +
                "\t\t\"editor\":#{editor},\n" +
                "\t\t\"comment\":#{comment},\n" +
                "\t\t\"datasource\":#{datasource},\n" +
                "\t\t\"script\":#{script},\n" +
                "\t\t\"create_time\":#{createTime},\n" +
                "\t\t\"update_time\":#{updateTime}\n" +
                "\t}]\n" +
                "}";
    }

    @Override
    public String updateApiInfoScript() {
        return "{\n" +
                "     update: \"api_info\",\n" +
                "     updates: \n" +
                "     \t[{\n" +
                "     \t\tq:{_id:ObjectId(#{id})},\n" +
                "     \t\tu:{$set:{method:#{method},path:#{path},datasource:#{datasource},group:#{group},editor:#{editor},comment:#{comment},script:#{script},update_time:#{updateTime}}},\n" +
                "     \t\tupsert:false,\n" +
                "     \t\tmulti:false\n" +
                "     \t}]\n" +
                "     \n" +
                "}";
    }

    @Override
    public String deleteApiInfoScript() {
        return "{\n" +
                "     delete: \"api_info\",\n" +
                "     deletes: [\n" +
                "     \t{q:{id:#{id},limit:1}}}\n" +
                "     ]\n" +
                "}";
    }

    @Override
    public Object execute(String script, ApiInfo apiInfo, ApiParams apiParams) {
        mongoTemplate.executeCommand(script);
        return null;
    }

    @Override
    public List<Map<String,Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams) {
        Document document = mongoTemplate.executeCommand(script);
        List<Document> documents = (List<Document>) ((Document)document.get("cursor")).get("firstBatch");
        return documents.stream().map(item->toMap(item)).collect(Collectors.toList());
    }

    private Map<String,Object> toMap(Document item) {
        Map<String, Object> map = new HashMap<>(item.size());
        Set<String> keys = item.keySet();
        for (String key : keys){
           Object value = item.get(key);
           if ("_id".equals(key)){
               key = "id";
           }
           key = super.underlineToCamel(key);
           if (value instanceof Document){
               map.put(key,toMap((Document) value));
               continue;
           }
            map.put(key,value instanceof ObjectId?((ObjectId)value).toHexString():value);
        }
        return map;
    }

    @Override
    public Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams) {
        Document document = mongoTemplate.executeCommand(script);
        return new Long(document.getInteger("n"));
    }


}

package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    public Object execute(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        formatISODate(script);
        mongoTemplate.executeCommand(script.toString());
        return null;
    }

    @Override
    public List<Map<String,Object>> executeQuery(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        formatISODate(script);
        Document document = mongoTemplate.executeCommand(script.toString());
        List<Document> documents = (List<Document>) ((Document)document.get("cursor")).get("firstBatch");
        return documents.stream().map(item->toMap(item)).collect(Collectors.toList());
    }

    @Override
    public Long executeCount(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        formatISODate(script);
        Document document = mongoTemplate.executeCommand(script.toString());
        return new Long(document.getInteger("n"));
    }

    public void formatISODate(StringBuilder script){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfDt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
        String flag = "ISODate(";
        for (int i=0;i<script.length();i++){
            int startIndex = script.indexOf(flag,i);
            if (startIndex == -1){
                break;
            }

            int endIndex = script.indexOf(")",startIndex+flag.length());
            if (endIndex == -1){
                throw new IllegalArgumentException("missed ISODate( close ')'");
            }

            String timeStr = script.substring(startIndex+flag.length()+1,endIndex-1);
            Date time = null;
            try {
                sdfUtc.parse(timeStr);
                i = endIndex;
                continue;
            } catch (ParseException e) {
                try {
                    time = sdf.parse(timeStr);
                } catch (ParseException ex) {
                    try {
                        time = sdfDt.parse(timeStr);
                    } catch (ParseException exx) {
                        throw new MissingFormatArgumentException("format ISODate error"+script.substring(startIndex,endIndex));
                    }
                }
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.HOUR_OF_DAY,-8);
            script = script.replace(startIndex+flag.length()+1,endIndex-1,sdfUtc.format(calendar.getTime()));
        }
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
}

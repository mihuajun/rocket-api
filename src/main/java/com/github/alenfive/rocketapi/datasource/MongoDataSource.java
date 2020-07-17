package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * mongodb 数据源操作
 */
public class MongoDataSource extends DataSourceDialect {

    private MongoTemplate mongoTemplate;


    private MongoDataSource(){}

    public MongoDataSource(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

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
    String lastApiInfoHistoryScript() {
        return "{\n" +
                "\t\"find\":\"api_info_history\",\n" +
                "\t\"filter\":{\n" +
                "\t\t\"service\":#{service}\n" +
                "\t\t,?{apiInfoId,\"api_info_id\":ObjectId(#{apiInfoId})}\n" +
                "\t}," +
                "sort:{_id:-1},\n" +
                "skip:#{index}\n" +
                "limit:#{pageSize}\n" +
                "}";
    }

    @Override
    public String saveApiInfoHistoryScript() {
        return "{\n" +
                "\t\"insert\":\"api_info_history\",\n" +
                "\t\"documents\":[{\n" +
                "\t\t\"api_info_id\":ObjectId(#{apiInfoId}),\n" +
                "\t\t\"method\":#{method},\n" +
                "\t\t\"path\":#{path},\n" +
                "\t\t\"type\":#{type},\n" +
                "\t\t\"service\":#{service},\n" +
                "\t\t\"group\":#{group},\n" +
                "\t\t\"editor\":#{editor},\n" +
                "\t\t\"comment\":#{comment},\n" +
                "\t\t\"datasource\":#{datasource},\n" +
                "\t\t\"script\":#{script},\n" +
                "\t\t\"options\":#{options},\n" +
                "\t\t\"create_time\":#{createTime},\n" +
                "\t}]\n" +
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
                "\t\t\"options\":#{options},\n" +
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
                "     \t\tu:{$set:{method:#{method},path:#{path},datasource:#{datasource},group:#{group},editor:#{editor},comment:#{comment},script:#{script},options:#{options},update_time:#{updateTime}}},\n" +
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
                "     \t{q:{_id:ObjectId(#{id})},limit:1}\n" +
                "     ]\n" +
                "}";
    }

    @Override
    public String saveApiExampleScript() {
        return "{\n" +
                "\t\"insert\":\"api_example\",\n" +
                "\t\"documents\":[{\n" +
                "\t\t\"api_info_id\":ObjectId(#{apiInfoId}),\n" +
                "\t\t\"method\":#{method},\n" +
                "\t\t\"url\":#{url},\n" +
                "\t\t\"request_header\":#{requestHeader},\n" +
                "\t\t\"request_body\":#{requestBody},\n" +
                "\t\t\"response_header\":#{responseHeader},\n" +
                "\t\t\"response_body\":#{responseBody},\n" +
                "\t\t\"status\":#{status},\n" +
                "\t\t\"time\":#{time},\n" +
                "\t\t\"editor\":#{editor},\n" +
                "\t\t\"options\":#{options},\n" +
                "\t\t\"create_time\":ISODate(#{createTime})\n" +
                "\t}]\n" +
                "}";
    }

    @Override
    public String lastApiExampleScript() {
        return "{\n" +
                "     find: \"api_example\",\n" +
                "     filter: { api_info_id: ObjectId(#{apiInfoId}) }," +
                "     sort:{_id:-1},\n" +
                "     limit: #{limit}\n" +
                "}";
    }

    @Override
    public String deleteExampleScript() {
        return "{\n" +
                "     delete: \"api_example\",\n" +
                "     deletes: [\n" +
                "     \t{q:{_id:{$in:[ObjectId(#{ids})]}},limit:0}\n" +
                "     ]\n" +
                "}";
    }


    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams)  throws Exception {
        formatISODate(script);
        formatObjectIdList(script);
        Document document = mongoTemplate.executeCommand(script.toString());
        if (document.get("n") != null){
            Map<String,Object> count = new HashMap<>();
            count.put("count",document.get("n"));
            List<Map<String,Object>> result = new ArrayList<>(1);
            result.add(count);
            return result;
        }
        List<Document> documents = (List<Document>) ((Document)document.get("cursor")).get("firstBatch");
        return documents.stream().map(item->toMap(item)).collect(Collectors.toList());
    }

    @Override
    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams)  throws Exception {
        formatISODate(script);
        formatObjectIdList(script);
        Document result = mongoTemplate.executeCommand(script.toString());
        return Long.valueOf(result.getInteger("n"));
    }

    @Override
    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        formatISODate(script);
        formatObjectIdList(script);
        Document result = mongoTemplate.executeCommand(script.toString());
        return Long.valueOf(result.getInteger("n"));
    }

    @Override
    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        formatISODate(script);
        formatObjectIdList(script);
        return batchInsert(script).get(0).toString();
    }

    @Override
    String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams) throws Exception {
        return script;
    }

    private List<Object> batchInsert(StringBuilder script){
        Document insertDoc = Document.parse(script.toString());
        List<Document> docList = getList(insertDoc,"documents",Document.class,null);
        if (CollectionUtils.isEmpty(docList)){
            throw new RuntimeException("insert documents is empty");
        }
        for (Document doc : docList ){
            ObjectId id = doc.getObjectId("_id");
            if (id == null){
                doc.put("_id",ObjectId.get());
            }
        }
        mongoTemplate.executeCommand(insertDoc);
        return docList.stream().map(item->item.get("_id")).collect(Collectors.toList());
    }

    private <T> List<T> getList(Document document,final Object key, final Class<T> clazz, final List<T> defaultValue) {
        List<?> value = document.get(key, List.class);
        if (value == null) {
            return defaultValue;
        }

        for (Object item : value) {
            if (!clazz.isAssignableFrom(item.getClass())) {
                throw new ClassCastException(format("List element cannot be cast to %s", clazz.getName()));
            }
        }
        return (List<T>) value;
    }

    private void formatObjectIdList(StringBuilder script) {
        String flag = "ObjectId(";
        for (int i=0;i<script.length();i++){
            int startIndex = script.indexOf(flag,i);
            if (startIndex == -1){
                break;
            }

            int endIndex = script.indexOf(")",startIndex+flag.length());
            if (endIndex == -1){
                throw new IllegalArgumentException("missed ObjectId( close ')'");
            }

            String objectIdStr = script.substring(startIndex+flag.length(),endIndex);
            String[] objectIdArr = objectIdStr.split(",");
            if (objectIdArr.length == 1){
                i = endIndex;
                continue;
            }
            String newObjectIds = Stream.of(objectIdArr).map(item->"ObjectId("+item+")").collect(Collectors.joining(","));
            i = startIndex + newObjectIds.length();
            script = script.replace(startIndex,endIndex+1,newObjectIds);
        }
    }

    public void formatISODate(StringBuilder script){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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


            try {
                sdfUtc.parse(timeStr);
                i = endIndex;
                continue;
            } catch (ParseException e) {}

            boolean isLocal = timeStr.endsWith("+08:00");
            if (isLocal){
                timeStr = timeStr.replace("+08:00","").replace("T"," ");
            }

            Date time = null;
            try {
                time = sdf1.parse(timeStr);
            } catch (ParseException e) {
                try {
                    time = sdf2.parse(timeStr);
                } catch (ParseException ex) {
                    try {
                        time = sdf3.parse(timeStr);
                    } catch (ParseException exx) {
                        throw new MissingFormatArgumentException("format ISODate error "+script.substring(startIndex,endIndex));
                    }
                }
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            calendar.add(Calendar.HOUR_OF_DAY,-8);

            String newTime = sdfUtc.format(calendar.getTime());
            i = startIndex+flag.length()+1 + newTime.length();
            script = script.replace(startIndex+flag.length()+1,endIndex-1,newTime);
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

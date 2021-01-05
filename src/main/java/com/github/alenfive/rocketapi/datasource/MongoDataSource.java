package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.ApiJpaUtil;
import com.github.alenfive.rocketapi.utils.FieldUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
    void saveApiInfo(ApiInfo apiInfo) {
        mongoTemplate.save(apiInfo, ApiJpaUtil.getApiTableName(ApiInfo.class));
    }

    @Override
    public ApiInfo findApiInfoById(ApiInfo apiInfo) {
        return mongoTemplate.findById(apiInfo.getId(),ApiInfo.class,ApiJpaUtil.getApiTableName(ApiInfo.class));
    }

    @Override
    public void deleteApiInfo(ApiInfo apiInfo) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(new ObjectId(apiInfo.getId()))),ApiJpaUtil.getApiTableName(ApiInfo.class));
    }

    @Override
    public void updateApiInfo(ApiInfo apiInfo) {
        mongoTemplate.save(apiInfo,ApiJpaUtil.getApiTableName(ApiInfo.class));
    }

    @Override
    public List<ApiInfo> listApiInfoByEntity(ApiInfo apiInfo) {
        Query query = Query.query(Criteria.where("service").is(apiInfo.getService()));
        return mongoTemplate.find(query,ApiInfo.class,ApiJpaUtil.getApiTableName(ApiInfo.class));
    }

    @Override
    public void saveApiInfoHistory(ApiInfoHistory apiInfoHistory) {
        mongoTemplate.save(apiInfoHistory,ApiJpaUtil.getApiTableName(ApiInfoHistory.class));
    }

    @Override
    public List<ApiInfoHistory> listApiInfoHistoryByEntity(ApiInfoHistory apiInfoHistory, IApiPager apiPager, Page page) {
        Criteria criteria = Criteria.where("service").is(apiInfoHistory.getService());
        if (!StringUtils.isEmpty(apiInfoHistory.getApiInfoId())){
            criteria.and("apiInfoId").is(apiInfoHistory.getApiInfoId());
        }
        Query query = Query.query(criteria);
        query.skip(apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo())).limit(page.getPageSize());
        query.with(Sort.by(Sort.Direction.DESC,"_id"));
        return mongoTemplate.find(query,ApiInfoHistory.class,ApiJpaUtil.getApiTableName(ApiInfoHistory.class));
    }

    @Override
    public void saveApiExample(ApiExample apiExample) {
        mongoTemplate.save(apiExample,ApiJpaUtil.getApiTableName(ApiExample.class));
    }

    @Override
    public List<ApiExample> listApiExampleByEntity(ApiExample apiExample, IApiPager apiPager, Page page) {
        Query query = Query.query(Criteria.where("apiInfoId").is(apiExample.getApiInfoId()));
        query.skip(apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo())).limit(page.getPageSize());
        query.with(Sort.by(Sort.Direction.DESC,"_id"));
        return mongoTemplate.find(query,ApiExample.class,ApiJpaUtil.getApiTableName(ApiExample.class));
    }

    @Override
    public void deleteExample(ApiExample apiExample) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(new ObjectId(apiExample.getId()))),ApiJpaUtil.getApiTableName(ApiExample.class));
    }

    @Override
    public void saveApiConfig(ApiConfig apiConfig) {
        mongoTemplate.save(apiConfig,ApiJpaUtil.getApiTableName(ApiConfig.class));
    }

    @Override
    public void updateApiConfig(ApiConfig apiConfig) {
        mongoTemplate.save(apiConfig,ApiJpaUtil.getApiTableName(ApiConfig.class));
    }

    @Override
    public List<ApiConfig> listApiConfigByEntity(ApiConfig apiConfig) {
        Query query = Query.query(Criteria.where("service").is(apiConfig.getService()));
        return mongoTemplate.find(query,ApiConfig.class,ApiJpaUtil.getApiTableName(ApiConfig.class));
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
        Document cursor = (Document) document.get("cursor");
        if (cursor != null){
            List<Document> documents = (List<Document>) cursor.get("firstBatch");

            while (Long.valueOf(cursor.get("id").toString()) > 0L){
                Document more = new Document();
                more.put("getMore",cursor.get("id"));
                more.put("collection",cursor.get("ns").toString().split("\\.")[1]);
                document = mongoTemplate.executeCommand(more);
                cursor = (Document) document.get("cursor");
                documents.addAll((List<Document>) cursor.get("nextBatch"));
            }
            return documents.stream().map(item->toMap(item)).collect(Collectors.toList());
        }
        return Arrays.asList(toMap(document));
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
           key = FieldUtils.underlineToCamel(key);
           if (value instanceof Document){
               map.put(key,toMap((Document) value));
               continue;
           }
            map.put(key,value instanceof ObjectId?((ObjectId)value).toHexString():value);
        }
        return map;
    }


    @Override
    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        Document document = Document.parse(script);
        document.put("count",document.get("find"));
        document.put("query",document.get("filter"));
        document.remove("find");
        document.remove("filter");
        document.remove("sort");
        return document.toJson();
    }

    @Override
    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        Document document = Document.parse(script);
        document.put("skip",apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo()));
        document.put("limit",page.getPageSize());
        return document.toJson();
    }

    @Override
    public String transcoding(String param) {
        return param
                .replace("\\","\\\\")
                .replace("\"","\\\"")
                .replace("\'","\\\'");
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

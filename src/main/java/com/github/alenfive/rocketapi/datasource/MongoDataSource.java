package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiEntity;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.ScriptContext;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.ApiAnnotationUtil;
import com.github.alenfive.rocketapi.utils.FieldUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
@SuppressWarnings("ALL")
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
    public <T extends ApiEntity> void saveEntity(T entity) {
        mongoTemplate.save(entity, ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }

    @Override
    public <T extends ApiEntity> T findEntityById(T entity) {
        return (T) mongoTemplate.findById(entity.getId(),entity.getClass(),ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }

    @Override
    public <T extends ApiEntity> void removeEntityById(T entity) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(new ObjectId(entity.getId()))),ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }

    @Override
    public <T extends ApiEntity> void updateEntityById(T entity) {
        mongoTemplate.save(entity,ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }

    @Override
    public <T extends ApiEntity> List<T> listByEntity(T entity) {
        Query query = Query.query(buildCriteria(entity));
        query.with(Sort.by(Sort.Direction.DESC,"_id"));
        return mongoTemplate.find(query,(Class <T>) (entity.getClass()),ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }

    private <T extends ApiEntity> Criteria buildCriteria(T entity){
        Criteria criteria = new Criteria();
        FieldUtils.allFields(entity.getClass()).stream().filter(item->{
            item.setAccessible(true);
            try {
                Object value = item.get(entity);
                return value != null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }).forEach(item->{
            try {
                criteria.and(item.getName()).is(item.get(entity));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        return criteria;
    }

    @Override
    public <T extends ApiEntity> List<T> pageByEntity(T entity, IApiPager apiPager, Page page) {
        Query query = Query.query(buildCriteria(entity));
        query.skip(apiPager.getOffset(page.getPageSize(),page.getPageNo())).limit(page.getPageSize());
        query.with(Sort.by(Sort.Direction.DESC,"_id"));
        return mongoTemplate.find(query,(Class <T>) (entity.getClass()),ApiAnnotationUtil.getApiTableName(entity.getClass()));
    }


    @Override
    public List<Map<String,Object>> find(ScriptContext scriptContext)  throws Exception {
        formatISODate(scriptContext.getScript());
        formatObjectIdList(scriptContext.getScript());
        Document document = mongoTemplate.executeCommand(scriptContext.getScript().toString());
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
    public int update(ScriptContext scriptContext) throws Exception {
        formatISODate(scriptContext.getScript());
        formatObjectIdList(scriptContext.getScript());
        Document result = mongoTemplate.executeCommand(scriptContext.getScript().toString());
        return result.getInteger("n");
    }

    @Override
    public int[] batchUpdate(ScriptContext scriptContext) throws Exception {
        return new int[0];
    }

    @Override
    public int remove(ScriptContext scriptContext) throws Exception {
        formatISODate(scriptContext.getScript());
        formatObjectIdList(scriptContext.getScript());
        Document result = mongoTemplate.executeCommand(scriptContext.getScript().toString());
        return result.getInteger("n");
    }

    @Override
    public Object insert(ScriptContext scriptContext) throws Exception {
        formatISODate(scriptContext.getScript());
        formatObjectIdList(scriptContext.getScript());
        return batchInsert(scriptContext.getScript()).get(0).toString();
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

    private Map<String,Object>  toMap(Document item) {
        Map<String, Object> map = new HashMap<>(item.size());
        Set<String> keys = item.keySet();
        for (String key : keys){
           Object value = item.get(key);
           if ("_id".equals(key)){
               key = "id";
           }
           if (value instanceof Document){
               map.put(key,toMap((Document) value));
               continue;
           }
            map.put(key,value instanceof ObjectId?((ObjectId)value).toHexString():value);
        }
        return map;
    }


    @Override
    public String buildCountScript(String script, IApiPager apiPager, Page page) {
        Document document = Document.parse(script);
        document.put("count",document.get("find"));
        document.put("query",document.get("filter"));
        document.remove("find");
        document.remove("filter");
        document.remove("sort");
        return document.toJson();
    }

    @Override
    public String buildPageScript(String script,IApiPager apiPager, Page page) {
        Document document = Document.parse(script);
        document.put("skip",apiPager.getOffset(page.getPageSize(),page.getPageNo()));
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
    public void close() {

    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}

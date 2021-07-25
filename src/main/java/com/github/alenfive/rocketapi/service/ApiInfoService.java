package com.github.alenfive.rocketapi.service;

import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.config.SysApiPager;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.*;
import com.github.alenfive.rocketapi.extend.IApiInfoCache;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiInfoService {

    @Autowired
    private IApiInfoCache apiInfoCache;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private RequestMappingService requestMappingService;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    private IApiPager apiPager = new SysApiPager();

    @Transactional
    public String saveApiInfo(ApiInfo apiInfo) throws Exception {

        RefreshMapping refreshMapping = new RefreshMapping();

        //更新时，历史记录为缓存记录
        if (!StringUtils.isEmpty(apiInfo.getId())){
            ApiInfo oldApiInfo = this.getApiInfoById(apiInfo.getId());
            refreshMapping.setOldMapping(MappingVo.builder().method(oldApiInfo.getMethod()).fullPath(oldApiInfo.getFullPath()).build());
        }

        buildFullPath(apiInfo);

        this.assertExistsPattern(apiInfo);

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item -> item.getId().equals(apiInfo.getId())).findFirst().orElse(null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        apiInfo.setUpdateTime(sdf.format(new Date()));
        if (dbInfo == null) {
            apiInfo.setType(ApiType.Ql.name());
            apiInfo.setCreateTime(sdf.format(new Date()));
            apiInfo.setService(rocketApiProperties.getServiceName());
            apiInfo.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);
        } else {
            apiInfo.setType(dbInfo.getType());
            apiInfo.setCreateTime(dbInfo.getCreateTime());
            apiInfo.setService(dbInfo.getService());

            dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);

            //取消mapping注册
            requestMappingService.unregisterMappingForApiInfo(dbInfo);

            //清理缓存
            apiInfoCache.remove(dbInfo);
        }

        dbInfo = dataSourceManager.getStoreApiDataSource().findEntityById(apiInfo);

        //入缓存
        apiInfoCache.put(dbInfo);

        //通知变更
        refreshMapping.setNewMapping(MappingVo.builder().method(apiInfo.getMethod()).fullPath(apiInfo.getFullPath()).build());
        //触发刷新
        apiInfoCache.refreshNotify(NotifyEntity.builder().eventType(NotifyEventType.RefreshMapping).refreshMapping(refreshMapping).build());

        //注册mapping
        requestMappingService.registerMappingForApiInfo(dbInfo);

        //存储历史
        saveApiHistory(dbInfo);

        return dbInfo.getId();
    }

    private void saveApiHistory(ApiInfo dbInfo) {
        ApiInfoHistory history = new ApiInfoHistory();
        BeanUtils.copyProperties(dbInfo, history);
        history.setApiInfoId(dbInfo.getId());
        history.setId(GenerateId.get().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        history.setCreateTime(sdf.format(new Date()));
        dataSourceManager.getStoreApiDataSource().saveEntity(history);
    }

    /**
     * 获取冲突的ApiInfo信息
     * @param apiInfo
     * @return
     */
    private List<ApiInfo> getConflictApiInfo(ApiInfo apiInfo) {
        return apiInfoCache.getAll().stream().filter(item->
                item.getFullPath().equals(apiInfo.getFullPath())
                        && (item.getMethod().equals("All") || item.getMethod().equals(apiInfo.getMethod()))
                        && !item.getId().equals(apiInfo.getId())).collect(Collectors.toList());
    }

    private void assertExistsPattern(ApiInfo apiInfo) {

        if (getConflictApiInfo(apiInfo).isEmpty() && !requestMappingService.isCodeMapping(apiInfo.getFullPath(),apiInfo.getMethod())){
            return;
        }
        throw new IllegalArgumentException("method: " + apiInfo.getMethod() + " path:" + apiInfo.getFullPath() + " already exist");
    }



    @Transactional
    public void deleteApiInfo(ApiInfo apiInfo) {

        ApiInfo dbInfo = this.getApiInfoById(apiInfo.getId());
        if (dbInfo == null) {
            return;
        }

        RefreshMapping refreshMapping = new RefreshMapping();
        refreshMapping.setOldMapping(MappingVo.builder().method(dbInfo.getMethod()).fullPath(dbInfo.getFullPath()).build());

        //清数据库
        dataSourceManager.getStoreApiDataSource().removeEntityById(apiInfo);

        //清缓存
        apiInfoCache.remove(dbInfo);

        //刷新通知
        apiInfoCache.refreshNotify(NotifyEntity.builder().eventType(NotifyEventType.RefreshMapping).refreshMapping(refreshMapping).build());

        //取消mapping注册
        requestMappingService.unregisterMappingForApiInfo(dbInfo);
    }



    @Transactional
    public Object saveExample(ApiExample apiExample) {
        dataSourceManager.getStoreApiDataSource().saveEntity(apiExample);
        return apiExample.getId();
    }

    public List<ApiExample> listApiExampleScript(String apiInfoId, Integer pageSize, Integer pageNo) {
        Page page = Page.builder().pageNo(pageNo).pageSize(pageSize).build();
        return dataSourceManager.getStoreApiDataSource().pageByEntity(ApiExample.builder().apiInfoId(apiInfoId).build(), apiPager, page);
    }

    @Transactional
    public void deleteExampleList(ArrayList<ApiExample> apiExampleList) {
        apiExampleList.stream().forEach(item -> {
            dataSourceManager.getStoreApiDataSource().removeEntityById(item);
        });
    }

    public List<ApiInfoHistory> lastApiInfo(String apiInfoId, Integer pageSize, Integer pageNo) {
        Page page = Page.builder().pageNo(pageNo).pageSize(pageSize).build();
        return dataSourceManager.getStoreApiDataSource().pageByEntity(ApiInfoHistory.builder().apiInfoId(apiInfoId).service(rocketApiProperties.getServiceName()).build(), apiPager, page);
    }



    /**
     * 数据导入
     *
     * @param directories
     * @param apiInfos
     * @param override
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public Object importAPI(Collection<ApiDirectory> directories, Collection<ApiInfo> apiInfos, Boolean override) throws Exception {

        Collection<ApiInfo> currApiInfos = this.getPathList(false);
        Collection<ApiDirectory> currDirectories = this.loadDirectoryList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //目录增量同步
        for (ApiDirectory directory : directories) {
            ApiDirectory dbDirectory = currDirectories.stream().filter(item -> item.getId().equals(directory.getId())).findFirst().orElse(null);
            if (dbDirectory == null) {
                dataSourceManager.getStoreApiDataSource().saveEntity(directory);
            } else {
                dataSourceManager.getStoreApiDataSource().updateEntityById(directory);
            }
        }

        for (ApiInfo apiInfo : apiInfos) {
            ApiInfo dbInfo = currApiInfos.stream().filter(item -> item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
            if (dbInfo == null) {

                //是否为覆盖导入
                if (override){
                    this.getConflictApiInfo(apiInfo).forEach(this::deleteApiInfo);
                }else{
                    assertExistsPattern(apiInfo);
                }

                apiInfo.setCreateTime(sdf.format(new Date()));
                apiInfo.setUpdateTime(sdf.format(new Date()));
                dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);
            } else {
                apiInfo.setUpdateTime(sdf.format(new Date()));
                dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);
            }

            //保存历史版本
            saveApiHistory(apiInfo);
        }

        return apiInfos.size();
    }


    @Transactional(rollbackFor = Exception.class)
    public Object apiInfoSync(Collection<ApiDirectory> directories,Collection<ApiInfo> apiInfos,Boolean increment) throws Exception {

        if (CollectionUtils.isEmpty(apiInfos)){
            return 0;
        }

        Collection<ApiInfo> currApiInfos = this.getPathList(false);
        Collection<ApiDirectory> currDirectories = this.loadDirectoryList();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //全量同步
        if(!increment){

            //删除历史目录
            for (ApiDirectory dbDirectory : currDirectories ){
                dataSourceManager.getStoreApiDataSource().removeEntityById(dbDirectory);
            }

            //新增历史目录
            for (ApiDirectory directory : directories){
                dataSourceManager.getStoreApiDataSource().saveEntity(directory);
            }

            //删除历史信息
            for (ApiInfo dbInfo : currApiInfos){
                this.deleteApiInfo(dbInfo);
            }
            //添加新信息
            for (ApiInfo apiInfo : apiInfos){
                apiInfo.setCreateTime(sdf.format(new Date()));
                apiInfo.setUpdateTime(sdf.format(new Date()));
                dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }else {

            //目录增量同步
            for (ApiDirectory directory : directories){
                ApiDirectory dbDirectory = currDirectories.stream().filter(item->item.getId().equals(directory.getId())).findFirst().orElse(null);
                if (dbDirectory == null){
                    dataSourceManager.getStoreApiDataSource().saveEntity(directory);
                }else{
                    dataSourceManager.getStoreApiDataSource().updateEntityById(directory);
                }
            }

            //增量同步
            for (ApiInfo apiInfo : apiInfos){
                ApiInfo dbInfo =  currApiInfos.stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
                if (dbInfo == null){

                    assertExistsPattern(apiInfo);

                    apiInfo.setCreateTime(sdf.format(new Date()));
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);
                }else{
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);
                }

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }

        return apiInfos.size();
    }


    public List<ApiDirectory> loadDirectoryList() {

        List<ApiDirectory> directoryList = dataSourceManager.getStoreApiDataSource().listByEntity(ApiDirectory.builder().service(rocketApiProperties.getServiceName()).build());

        //添加默认组
        if (directoryList.isEmpty()){
            try {
                this.saveDirectory(ApiDirectory.builder()
                        .name("默认组")
                        .service(rocketApiProperties.getServiceName())
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
            directoryList = dataSourceManager.getStoreApiDataSource().listByEntity(ApiDirectory.builder().service(rocketApiProperties.getServiceName()).build());
        }

        return directoryList;
    }

    /**
     * 目录删除
     * @param directory
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeDirectory(ApiDirectory directory){
        List<ApiDirectory> directoryList = this.loadDirectoryList();
        //查询该节点以下级所有目录
        Set<String> directoryIds = findChildrenIds(directoryList,directory.getId());
        directoryIds.add(directory.getId());

        for (String directoryId: directoryIds){

            //目录清理
            ApiDirectory dir = new ApiDirectory();
            dir.setId(directoryId);
            dataSourceManager.getStoreApiDataSource().removeEntityById(dir);

            //目录下的api列表
            List<ApiInfo> apiInfoList = apiInfoCache.getAll().stream().filter(item->directoryId.equals(item.getDirectoryId())).collect(Collectors.toList());

            for (ApiInfo apiInfo : apiInfoList ){
                dataSourceManager.getStoreApiDataSource().removeEntityById(apiInfo);

                //取消mapping注册
                requestMappingService.unregisterMappingForApiInfo(apiInfo);

                //缓存清理
                apiInfoCache.remove(apiInfo);
            }
        }

    }

    /**
     * 目录修改
     * @param directory
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDirectory(ApiDirectory directory) throws Exception {

        directory.setService(rocketApiProperties.getServiceName());
        //新增
        if (StringUtils.isEmpty(directory.getId())){
            directory.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(directory);
            return;
        }

        ApiDirectory dbDirectory = dataSourceManager.getStoreApiDataSource().findEntityById(directory);

        //数据库更新
        dataSourceManager.getStoreApiDataSource().updateEntityById(directory);

        //如果path未发生修改
        if(Objects.equals(dbDirectory.getPath(),directory.getPath())){
            return;
        }

        //更新该目录下的所有api path
        List<ApiDirectory> directoryList = this.loadDirectoryList();
        Set<String> directoryIds = findChildrenIds(directoryList,directory.getId());
        directoryIds.add(directory.getId());
        List<ApiInfo> modifyApiInfos = apiInfoCache.getAll().stream().filter(item->directoryIds.contains(item.getDirectoryId()) && ApiType.Ql.name().equals(item.getType())).collect(Collectors.toList());

        //
        modifyApiInfos.forEach(item->{

            //数据库更新
            ApiInfo newApiInfo = new ApiInfo();
            BeanUtils.copyProperties(item,newApiInfo);

            //构建完整路径
            this.buildFullPath(newApiInfo);

            //验证是否路径是否冲突
            assertExistsPattern(newApiInfo);

            dataSourceManager.getStoreApiDataSource().updateEntityById(newApiInfo);
        });

        //缓存刷新
        for (ApiInfo item : modifyApiInfos){
            //取消mapping注册
            requestMappingService.unregisterMappingForApiInfo(item);

            //删除缓存
            apiInfoCache.remove(item);

            //刷新更新
            ApiInfo dbInfo = dataSourceManager.getStoreApiDataSource().findEntityById(item);
            apiInfoCache.put(dbInfo);

            //添加mapping注册
            requestMappingService.registerMappingForApiInfo(dbInfo);

            //存储历史
            saveApiHistory(dbInfo);
        }
    }

    private String formatPath(StringBuilder path){
        path.insert(0,"/");
        String result = path.toString().replaceAll("/+","/");
        if (result.length()>1 && result.endsWith("/")){
            result = result.substring(0,result.length()-1);
        }
        return result;
    }

    private void buildFullPath(ApiInfo apiInfo){
        StringBuilder path = new StringBuilder(apiInfo.getPath());
        this.recursiveFullPath(this.loadDirectoryList(),apiInfo.getDirectoryId(),path);
        apiInfo.setFullPath(this.formatPath(path));
    }
    private void recursiveFullPath(List<ApiDirectory> directoryList, String directoryId, StringBuilder path){
        ApiDirectory directory = directoryList.stream().filter(item->item.getId().equals(directoryId)).findFirst().orElse(null);

        if (!StringUtils.isEmpty(directory.getPath())){
            path.insert(0,"/");
            path.insert(0,directory.getPath());
        }

        if (StringUtils.isEmpty(directory.getParentId())){
            return;
        }
        this.recursiveFullPath(directoryList,directory.getParentId(),path);
    }

    public void relationParentDirectory(Set<ApiDirectory> directorySet,List<ApiDirectory> directoryList,ApiDirectory directory){
        directorySet.add(directory);
        for (ApiDirectory item : directoryList){
            if (item.getId().equals(directory.getParentId())){
                directorySet.add(directory);
                this.relationParentDirectory(directorySet,directoryList,item);
            }
        }
    }

    private Set<String> findChildrenIds(List<ApiDirectory> directoryList,String directoryId){
        Set<String> result = directoryList.stream().filter(item->directoryId.equals(item.getParentId())).map(ApiDirectory::getId).collect(Collectors.toSet());
        for (String item : result){
            result.addAll(this.findChildrenIds(directoryList,item));
        }
        return result;
    }

    private ApiInfo getApiInfoByMapping(String fullPath,String method){
        return apiInfoCache.getAll().stream().filter(item ->
                item.getFullPath().equals(fullPath)
                        && (item.getMethod().equals("All") || item.getMethod().equals(method))).findAny().orElse(null);
    }

    public ApiInfo getApiInfoById(String id){
        return apiInfoCache.getAll().stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public void reLoadApiInfo() throws NoSuchMethodException {
        //历史缓存清理
        if (apiInfoCache != null) {
            apiInfoCache.getAll().stream().filter(item -> ApiType.Ql.name().equals(item.getType())).forEach(item -> {
                requestMappingService.unregisterMappingForApiInfo(item);
            });
            apiInfoCache.removeAll();
        }

        //获取数据库信息
        List<ApiInfo> apiInfos = dataSourceManager.getStoreApiDataSource().listByEntity(ApiInfo.builder().service(rocketApiProperties.getServiceName()).build());
        for (ApiInfo apiInfo : apiInfos) {
            apiInfoCache.put(apiInfo);
        }

        //注册mapping
        for (ApiInfo apiInfo : apiInfoCache.getAll()) {
            requestMappingService.registerMappingForApiInfo(apiInfo);
        }
    }

    public Collection<ApiInfo> getPathList(boolean isDb) throws Exception {
        if (isDb) {
            reLoadApiInfo();
        }
        return apiInfoCache.getAll();
    }

    public ExportRes exportApi(ExportReq exportReq) throws Exception {

        List<String> apiInfoIds = Arrays.asList(exportReq.getApiInfoIds().split(","));

        Collection<ApiInfo> apiInfos = null;

        List<ApiDirectory> directories = this.loadDirectoryList();

        apiInfos = this.getPathList(false).stream().filter(item->apiInfoIds.contains(item.getId())).collect(Collectors.toList());

        Set<ApiDirectory> directorySet = new HashSet<>();
        for (ApiInfo apiInfo : apiInfos){
            ApiDirectory directory = directories.stream().filter(item->item.getId().equals(apiInfo.getDirectoryId())).findFirst().orElse(null);
            this.relationParentDirectory(directorySet,directories,directory);
        }

        return ExportRes.builder()
                .apiInfos(apiInfos)
                .directories(directorySet)
                .build();
    }
}

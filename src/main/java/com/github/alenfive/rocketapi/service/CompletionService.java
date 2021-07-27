package com.github.alenfive.rocketapi.service;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.factory.IDataSourceDialectDriver;
import com.github.alenfive.rocketapi.entity.vo.CompletionResult;
import com.github.alenfive.rocketapi.entity.vo.MethodVo;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.function.IFunction;
import com.github.alenfive.rocketapi.utils.PackageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CompletionService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSourceManager dataSourceManager;

    private Map<String,Object> cache = new ConcurrentHashMap<>();

    public CompletionResult provideCompletionTypes() throws Exception {
        String cacheKey = "completion-items-cache";
        CompletionResult result = null;
        if ((result = (CompletionResult) cache.get(cacheKey)) != null){
            return result;
        }

        result = new CompletionResult();
        Map<String, List<MethodVo>> clazzs = new LinkedHashMap<>();
        Map<String,String> variables = new HashMap<>();
        Map<String,String> syntax = new HashMap<>();
        Map<String,List<TableInfo>> dbInfos = new HashMap<>();
        result.setClazzs(clazzs);
        result.setVariables(variables);
        result.setSyntax(syntax);
        result.setDbInfos(dbInfos);

        //获取内置自定义函数变量
        Collection<IFunction> functionList = context.getBeansOfType(IFunction.class).values();
        functionList.forEach(item->{
            variables.put(item.getVarName(),item.getClass().getName());
        });

        //spring bean对象获取
        Map<String,Object> beans = context.getBeansOfType(Object.class);

        for (String key : beans.keySet()){
            buildClazz(clazzs,beans.get(key).getClass());
        }

        //本包JAVA类
        List<Class> classList = PackageUtil.loadClassByLoader(Thread.currentThread().getContextClassLoader());
        for (Class clazz : classList){
            buildClazz(clazzs,clazz);
        }

        //基础包 java.util java类
        List<String> classNames = PackageUtil.scan();
        for (String clazz : classNames){
            buildClazz(clazzs,clazz);
        }

        //常用语法提示
        syntax.put("foreach","for(item in ${1:collection}){\n\t\n}");
        syntax.put("fori","for(${1:i}=0;${1:i}<;${1:i}++){\n\t\n}");
        syntax.put("for","for(${1}){\n\t\n}");
        syntax.put("if","if(${1:condition}){\n\n}");
        syntax.put("ifelse","if(${1:condition}){\n\t\n}else{\n\t\n}");
        syntax.put("import","import ");
        syntax.put("continue","continue;");
        syntax.put("break","break;");

        //数据库信息获取
        Map<String, DataSourceDialect> dataSourceDialectMap = dataSourceManager.getDialectMap();
        dataSourceDialectMap.forEach((key,value)->{
            List<TableInfo> tableInfos = value.buildTableInfo();
            if (tableInfos != null){
                dbInfos.put(key,tableInfos);
            }
        });

        //常用工具类获取

        cache.put(cacheKey,result);
        return result;
    }

    private void buildClazz(Map<String, List<MethodVo>> clazzs, String clazz) {
        if (clazzs.get(clazz) != null || clazz.indexOf("$") !=-1){
            return;
        }
        clazzs.put(clazz,Collections.EMPTY_LIST);
    }

    private void buildClazz(Map<String, List<MethodVo>> clazzs, Class clazz) {
        if (clazzs.get(clazz.getName()) != null || clazz.getName().indexOf("$") !=-1){
            return;
        }
        clazzs.put(clazz.getName(),buildMethod(clazz));
    }

    public List<MethodVo> buildMethod(Class clazz){
        List<MethodVo> methodVos = new ArrayList<>();
        //成员变量
        for(Field field : clazz.getFields()){
            methodVos.add(MethodVo.builder()
                    .type("field")
                    .varName(field.getName())
                    .resultType(field.getType().getName())
                    .build());
        }

        //方法
        for (Method method : clazz.getMethods()){
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            String params = Stream.of(method.getParameters()).map(item->item.getType().getSimpleName()+" "+item.getName()).collect(Collectors.joining(","));
            methodVos.add(MethodVo.builder()
                    .type(isStatic?"static":"public")
                    .varName(method.getName())
                    .params(params)
                    .resultType(method.getReturnType().getName())
                    .build());
        }
        return methodVos;
    }

    public Collection<IDataSourceDialectDriver> getDriver() {
        return context.getBeansOfType(IDataSourceDialectDriver.class).values();
    }
}

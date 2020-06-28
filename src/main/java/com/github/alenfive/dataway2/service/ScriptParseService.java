package com.github.alenfive.dataway2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.dataway2.entity.ApiParams;
import com.github.alenfive.dataway2.entity.ParamScope;
import com.github.alenfive.dataway2.entity.vo.ArrVar;
import com.github.alenfive.dataway2.entity.vo.IndexScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/25 10:00
 * @UpdateDate: 2020/5/25 10:00
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 脚本解析器
 */

@Service
public class ScriptParseService {

    @Autowired
    private ObjectMapper objectMapper;

    private Set<String> scopeSet = Stream.of(ParamScope.values()).map(ParamScope::name).collect(Collectors.toSet());

    public void parse(StringBuilder script,ApiParams apiParams){
        buildMutilStr(script);
        buildIf(script,apiParams);
        buildParams(script,apiParams);
    }

    /**多行文本替换
     *  """
     *  str
     *  """
     * @param script
     */
    private void buildMutilStr(StringBuilder script) {
        IndexScope scope = null;
        String tokenFlag = "\"\"\"";
        while ((scope = buildIndexScope(script,tokenFlag,tokenFlag)) != null ){
            String newToken = scope.getToken()
                    .replace(tokenFlag,"")
                    .replace("\"","\\\"")
                    .replace("\r\n","\"+\r\n\"");
            script.replace(scope.getBeginIndex(),scope.getEndIndex()+1,"\""+newToken+"\"");
        }
    }


    /**
     * 构建FOR语法
     * @param script
     * @param apiParams
     * @return
     */
    public String buildFor(String script,ApiParams apiParams){
        return null;
    }

    /**
     * 查找开始截止位置，非递归或嵌套
     * @param source
     * @param beginToken
     * @param endToken
     * @return
     */
    public IndexScope buildIndexScope(StringBuilder source,String beginToken,String endToken){

        Integer beginIndex = -1;
        Integer endIndex = -1;
        beginIndex = source.indexOf(beginToken);
        if (beginIndex == -1){
            return null;
        }

        endIndex = source.indexOf(endToken,beginIndex + beginToken.length());
        if (endIndex == -1){
            throw new IllegalArgumentException("missed "+beginToken+" close '"+endToken+"'");
        }
        IndexScope indexScope = new IndexScope();
        indexScope.setBeginIndex(beginIndex);
        indexScope.setEndIndex(endIndex+endToken.length()-1);
        indexScope.setToken(source.substring(beginIndex,endIndex+endToken.length()));
        return indexScope;
    }


    /**
     * 构建IF语法
     * @param script
     * @param apiParams
     * @return
     */
    public void buildIf(StringBuilder script,ApiParams apiParams){
        String flag = "?{";
        //匹配参数#{}
        do{
            int startIf = script.indexOf(flag);
            if (startIf == -1){
                break;
            }

            int endIf = -1;
            int ifClose = 1;
            int ifSplit = -1;

            for(int i=startIf+flag.length();i<script.length();i++){
                char c = script.charAt(i);
                if (c == '{'){
                    ifClose ++ ;
                }
                if (c == '}' && --ifClose == 0){
                    endIf = i;
                    break;
                }
                if (ifSplit == -1 && c == ','){
                    ifSplit = i;
                }
            }

            if (endIf == -1){
                throw new IllegalArgumentException("missed if close '}'");
            }

            if (ifSplit == -1){
                throw new IllegalArgumentException("missed if split ','");
            }
            String varName = script.substring(startIf+flag.length(),ifSplit);
            Object value = buildParamItem(apiParams,varName);
            if (StringUtils.isEmpty(value)){
                script = script.replace(startIf,endIf+1,"");
            }else{
                script = script.replace(startIf,endIf+1,script.substring(ifSplit+1,endIf));
            }
        }while (true);

    }
    /**
     * 构建参数 #{}
     * @param script
     * @param apiParams
     * @return
     */
    public void buildParams(StringBuilder script, ApiParams apiParams){
        //匹配参数#{}
        Pattern r = Pattern.compile("#\\{[A-Za-z0-9-\\[\\]_\\.]+\\}");

        boolean find;
        do{
            Matcher m = r.matcher(script);
            find = m.find();
            if (find){
                String group = m.group();
                String varName = group.replace("#{","").replace("}","");
                Object value = buildParamItem(apiParams,varName);
                if (value == null){
                    script = script.replace(m.start(),m.end(),"null");
                }else{
                    script = script.replace(m.start(),m.end(),buildValue(value));
                }
            }
        }while (find);

        //匹配参数${}
        r = Pattern.compile("\\$\\{[A-Za-z0-9-\\[\\]_\\.]+\\}");
        do{
            Matcher m = r.matcher(script);
            find = m.find();
            if (find){
                String group = m.group();
                String varName = group.replace("${","").replace("}","");
                Object value = buildParamItem(apiParams,varName);
                if (value == null){
                    script = script.replace(m.start(),m.end(),"null");
                }else{
                    script = script.replace(m.start(),m.end(),buildSourceValue(value));
                }
            }
        }while (find);
    }

    public Object buildParamItem(ApiParams apiParams, String varName) {
        String[] paramArr = varName.split("\\.");


        Object value = null;
        if (scopeSet.contains(paramArr[0])){
            switch (ParamScope.valueOf(paramArr[0])){
                case pathVar:value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[1]);break;
                case param:value = buildValueOfParameter(apiParams.getParam(),paramArr,1);break;
                case body:value = buildValueOfBody(apiParams.getBody(),paramArr,1);break;
                case cookie:value = buildValueOfCookie(apiParams.getCookie(),apiParams.getRequest(),paramArr[1]);break;
                case header:value = buildValueOfHeader(apiParams.getHeader(),paramArr,1);break;
            }
        }else {
            value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[0]);
            if (value == null) {
                value = buildValueOfParameter(apiParams.getParam(), paramArr,0);
            }
            if(value == null){
                value = buildValueOfBody(apiParams.getBody(),paramArr, 0);
            }
            if(value == null){
                value = buildValueOfCookie(apiParams.getCookie(),apiParams.getRequest(), paramArr[0]);
            }
            if(value == null){
                value = buildValueOfHeader(apiParams.getHeader(),paramArr,0);
            }
        }
        return value;
    }

    private Object buildValueOfHeader(Map<String,String> header,String[] paramArr,int index) {
        String value  = null;
        if (header != null){
            value = header.get(paramArr[index]);
        }

        if (value == null)return null;

        if (paramArr.length-1 > index){
            try {
                return buildValueOfHeader(objectMapper.readValue(value,Map.class),paramArr,++index);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Parameter '"+String.join(".",paramArr)+"' is not an object");
            }
        }

        return value;
    }

    private Object buildValueOfCookie(Map<String,Object> cookie,HttpServletRequest request, String varName) {
        Object value  = null;
        if (cookie != null){
            value = cookie.get(varName);
        }

        if (request != null && value == null){
            Cookie[] cookies = request.getCookies();
            if (cookies == null)return null;
            for(Cookie item : cookies){
                if(item.getName().equals(varName)){
                    value = item.getValue();
                    break;
                }
            }
        }

        return value;
    }

    private Object buildValueOfBody(Map<String,Object> body, String[] paramArr,int index) {
        if (body == null)return null;

        Object value = buildObjectValue(body,paramArr[index]);
        if (paramArr.length-1 > index){
            return buildValueOfBody((Map<String, Object>) value,paramArr,++index);
        }
        return value;
    }

    private Object buildValueOfParameter(Map<String,Object> params, String[] paramArr,int index) {
        if (params == null)return null;

        Object value = buildObjectValue(params,paramArr[index]);
        if (paramArr.length-1 > index){
            return buildValueOfParameter((Map<String, Object>) value,paramArr,++index);
        }
        return value;
    }

    private Object buildObjectValue(Map<String,Object> params,String varName){
        Object value = null;
        ArrVar arrVar = isArrVar(varName);
        if (arrVar != null){
            Object collection = params.get(arrVar.getVarName());
            if (!(collection instanceof Collection)){
                throw new IllegalArgumentException("The "+arrVar.getVarName()+" parameter is not an array");
            }

            Collection list = ((Collection)collection);
            if (arrVar.getIndex() >=list.size()){
                throw new IllegalArgumentException("The parameter "+arrVar.getVarName()+" exceeds the array length");
            }
            value = list.toArray()[arrVar.getIndex()];
        }else{
            value = params.get(varName);
        }
        return value;
    }

    private ArrVar isArrVar(String varName){
        boolean isArrVar = varName.matches(".+\\[\\d+\\]$");
        if (!isArrVar)return null;
        String varNameFinal = varName.substring(0,varName.indexOf("["));
        Integer index = Integer.valueOf(varName.substring(varName.indexOf("[")+1,varName.length()-1));
        return new ArrVar(varNameFinal,index);
    }

    private Object buildValueOfPathVar(Map<String,String> pathVars, String varName) {
        if (pathVars == null)return null;
        return pathVars.get(varName);
    }

    private String buildSourceValue(Object val) {
        if (val == null)return null;
        StringBuilder valStr = new StringBuilder();
        if (val instanceof Collection){
            valStr.append(((Collection)val).stream().map(item->item.toString()).collect(Collectors.joining(",")));
        }else {
            valStr.append(val);
        }
        return valStr.toString();
    }

    private String buildValue(Object val) {
        if (val == null)return null;
        StringBuilder valStr = new StringBuilder();
        if (val instanceof Collection){
            valStr.append(((Collection)val).stream().map(item->buildStrValue(item)).collect(Collectors.joining(",")));
        }else {
            valStr.append(buildStrValue(val));
        }
        return valStr.toString();
    }

    private String buildStrValue(Object val){
        if (val == null)return null;
        if (val instanceof Number){
            return val.toString();
        }
        return "'"+val.toString()+"'";
    }
}

package com.github.alenfive.dataway2.service;

import com.github.alenfive.dataway2.entity.ApiParams;
import com.github.alenfive.dataway2.entity.ParamScope;
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

    private Set<String> scopeSet = Stream.of(ParamScope.values()).map(ParamScope::name).collect(Collectors.toSet());

    public void parse(StringBuilder script,ApiParams apiParams){
        buildIf(script,apiParams);
        buildParams(script,apiParams);
    }


    /**
     * 提取可执行脚本
     * 去掉注释
     */
    public List<StringBuilder> extractExecutableScript(String script) throws UnsupportedEncodingException {
        script = URLDecoder.decode(script,"utf-8");
        String[] scriptArr = script
                .replaceAll("//.*","")
                .replaceAll("\n","")
                .replaceAll("\t","")
                .replaceAll(" {2,}"," ").split("---");
        return Stream.of(scriptArr).map(item->new StringBuilder(item.trim())).collect(Collectors.toList());
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
     * 构建IF语法
     * @param script
     * @param apiParams
     * @return
     */
    public void buildIf(StringBuilder script,ApiParams apiParams){
        Set<String> scopeSet = Stream.of(ParamScope.values()).map(ParamScope::name).collect(Collectors.toSet());

        //匹配参数#{}
        do{
            int startIf = script.indexOf("#?{");
            if (startIf == -1){
                break;
            }

            int endIf = -1;
            int ifClose = 1;
            int ifSplit = -1;

            for(int i=startIf+3;i<script.length();i++){
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
            String varName = script.substring(startIf+3,ifSplit);
            Object value = buildParamItem(apiParams,varName);
            if (StringUtils.isEmpty(value)){
                script = script.replace(startIf,endIf+1,"");
            }else{
                script = script.replace(startIf,endIf+1,script.substring(ifSplit+1,endIf));
            }
        }while (true);

    }

    /**
     * 构建参数
     * @param script
     * @param apiParams
     * @return
     */
    public void buildParams(StringBuilder script, ApiParams apiParams){

        //匹配参数#{}
        Pattern r = Pattern.compile("#\\{[A-Za-z0-9-_\\.]+\\}");

        boolean find;
        do{
            Matcher m = r.matcher(script);
            find = m.find();
            if (find){
                String group = m.group();
                String varName = group.replace("#{","").replace("}","");
                Object value = buildParamItem(apiParams,varName);
                if (value == null){
                    throw new IllegalArgumentException("parameter '"+varName+"' not found");
                }

                script = script.replace(m.start(),m.end(),buildValue(value));
            }
        }while (find);

    }

    public Object buildParamItem(ApiParams apiParams, String varName) {
        String[] paramArr = varName.split("\\.");


        Object value = null;
        if (scopeSet.contains(paramArr[0])){
            switch (ParamScope.valueOf(paramArr[0])){
                case pathVar:value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[1]);break;
                case param:value = buildValueOfParameter(apiParams.getParam(),paramArr[1]);break;
                case body:value = buildValueOfBody(apiParams.getBody(),paramArr,1);break;
                case cookie:value = buildValueOfCookie(apiParams.getRequest(),paramArr[1]);break;
                case header:value = buildValueOfHeader(apiParams.getRequest(),paramArr,1);break;
            }
        }else {
            value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[0]);
            if (value == null) {
                value = buildValueOfParameter(apiParams.getParam(), paramArr[0]);
            }
            if(value == null){
                value = buildValueOfBody(apiParams.getBody(),paramArr, 0);
            }
            if(value == null){
                value = buildValueOfCookie(apiParams.getRequest(), paramArr[0]);
            }
            if(value == null){
                value = buildValueOfHeader(apiParams.getRequest(),paramArr,0);
            }
        }
        return value;
    }

    private Object buildValueOfHeader(HttpServletRequest request, String[] paramArr,int index) {
        if (request == null)return null;
        return request.getHeader(paramArr[index]);
    }

    private Object buildValueOfCookie(HttpServletRequest request, String varName) {
        if (request == null)return null;
        Cookie[] cookies = request.getCookies();
        if (cookies == null)return null;
        Object value  = null;
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(varName)){
                value = cookie.getValue();
                break;
            }
        }
        return value;
    }

    private Object buildValueOfBody(Map<String,Object> body, String[] paramArr,int index) {
        if (body == null)return null;
        Object value = body.get(paramArr[index]);
        if (paramArr.length-1 > index){
            return buildValueOfBody((Map<String, Object>) value,paramArr,++index);
        }
        return value;
    }

    private Object buildValueOfParameter(Map<String,Object> params, String varName) {
        if (params == null)return null;
        return params.get(varName);
    }

    private Object buildValueOfPathVar(Map<String,Object> pathVars, String varName) {
        if (pathVars == null)return null;
        return pathVars.get(varName);
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

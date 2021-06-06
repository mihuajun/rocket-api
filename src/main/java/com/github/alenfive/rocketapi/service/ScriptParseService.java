package com.github.alenfive.rocketapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.ParamScope;
import com.github.alenfive.rocketapi.entity.vo.ArrVar;
import com.github.alenfive.rocketapi.entity.vo.IndexScope;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.script.GroovyScriptParse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.script.Bindings;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据库脚本变量解析器，用于捡的变量#{},?{},${}等
 */

@SuppressWarnings("DuplicatedCode")
@Service
public class ScriptParseService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Lazy
    private GroovyScriptParse scriptParse;

    @Autowired
    private ApiInfoContent apiInfoContent;

    private Set<String> scopeSet = Stream.of(ParamScope.values()).map(ParamScope::name).collect(Collectors.toSet());

    public void parse(StringBuilder script, ApiParams apiParams, DataSourceDialect sourceDialect,Map<String,Object> specifyParams) {
        buildIf(script,apiParams,specifyParams);
        buildParams(script,apiParams,sourceDialect,specifyParams);
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
     */
    public String buildFor(String script,ApiParams apiParams){
        return null;
    }

    /**
     * 查找开始截止位置，非递归或嵌套
     * @param source
     * @param beginToken
     * @param endToken
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
     */
    public void buildIf(StringBuilder script,ApiParams apiParams,Map<String,Object> specifyParams) {
        String flag = "?{";
        //匹配参数#{}
        do{
            int startIf = script.indexOf(flag);
            if (startIf == -1){
                break;
            }


            int ifSplitIndex = -1;
            int ifCloseIndex = -1;

            int quotationMark = 0;
            int bigBracket = 1;
            int roundBracket = 0;
            int squareBracket = 0;

            for(int i=startIf+flag.length();i<script.length();i++){
                char c = script.charAt(i);

                if (quotationMark > 0){
                    if (c == '\\') {
                        i++;
                        continue;
                    }
                    if (c == '"'){
                        quotationMark --;
                    }
                    continue;
                }

                if (c == '"'){
                    quotationMark ++;
                    continue;
                }


                if (c == '{'){
                    bigBracket ++ ;
                }
                if (c == '('){
                    roundBracket ++ ;
                }
                if (c == '['){
                    squareBracket ++ ;
                }
                if (c == '}'){
                    bigBracket -- ;
                }
                if (c == ')'){
                    roundBracket -- ;
                }
                if (c == ']'){
                    squareBracket -- ;
                }

                if (ifSplitIndex == -1 && c == ',' && bigBracket == 1 && roundBracket == 0 && squareBracket == 0){
                    ifSplitIndex = i;
                }

                if (c == '}' && bigBracket == 0){
                    ifCloseIndex = i;
                    break;
                }

            }

            if (ifCloseIndex == -1){
                throw new IllegalArgumentException("missed if close '}'");
            }

            if (ifSplitIndex == -1){
                throw new IllegalArgumentException("missed if split ','");
            }
            String condition = script.substring(startIf+flag.length(),ifSplitIndex);

            Object value = null;
            if (Pattern.matches("^\\w+$", condition)){
                value = buildParamItem(apiParams,specifyParams,condition);
            }else {
                try {
                    value = scriptParse.engineEval(condition, apiInfoContent.getEngineBindings());
                }catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }

            if (StringUtils.isEmpty(value) || (value instanceof Boolean && !(Boolean)value)){
                script = script.replace(startIf,ifCloseIndex+1,"");
            }else{
                script = script.replace(startIf,ifCloseIndex+1,script.substring(ifSplitIndex+1,ifCloseIndex));
            }
        }while (true);

    }
    /**
     * 构建参数 #{}
     * @param script
     * @param apiParams
     */
    public void buildParams(StringBuilder script, ApiParams apiParams,DataSourceDialect sourceDialect,Map<String,Object> specifyParams){
        //匹配参数#{}
        Pattern r = Pattern.compile("(#|\\$)\\{[A-Za-z0-9-\\[\\]_\\.]+\\}");

        Matcher m = r.matcher(script);
        int start = 0;
        while (m.find(start)){
            String group = m.group();
            if (group.startsWith("#")){
                String varName = group.replace("#{","").replace("}","");
                Object value = buildParamItem(apiParams,specifyParams,varName);
                String replaceValue = buildValue(value,sourceDialect);
                if (replaceValue == null){
                    replaceValue = "null";
                }
                script = script.replace(m.start(),m.end(),replaceValue);
                start = m.start() + replaceValue.length();
            }else if(group.startsWith("$")){
                String varName = group.replace("${","").replace("}","");
                Object value = buildParamItem(apiParams,specifyParams,varName);
                String replaceValue = buildSourceValue(value);
                if (replaceValue == null){
                    replaceValue = "null";
                }
                script = script.replace(m.start(),m.end(),replaceValue);
                start = m.start() + replaceValue.length();
            }

        }
    }

    public Object buildParamItem(ApiParams apiParams,Map<String,Object> specifyParams, String varName) {
        String[] paramArr = varName.split("\\.");

        if (specifyParams != null){
            return buildObjectValue(specifyParams,paramArr,0,varName);
        }

        Object value = null;
        if (scopeSet.contains(paramArr[0])){
            switch (ParamScope.valueOf(paramArr[0])){
                case content:value = buildValueOfScriptContent(apiInfoContent.getEngineBindings() == null?null:apiInfoContent.getEngineBindings(),paramArr,1);break;
                case pathVar:value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[1]);break;
                case param:value = buildValueOfParameter(apiParams.getParam(),paramArr,1);break;
                case body:value = buildValueOfBody(apiParams.getBody(),paramArr,1);break;
                case cookie:value = buildValueOfCookie(apiParams.getCookie(),apiParams.getRequest(),paramArr,1);break;
                case header:value = buildValueOfHeader(apiParams.getHeader(),paramArr,1);break;
                case session:value = buildValueOfSession(apiParams.getSession(),paramArr,1);break;
            }
        }else {
            value = buildValueOfScriptContent(apiInfoContent.getEngineBindings() == null?null:apiInfoContent.getEngineBindings(),paramArr,0);
            if (value == null){
                value = buildValueOfPathVar(apiParams.getPathVar(),paramArr[0]);
            }
            if (value == null) {
                value = buildValueOfParameter(apiParams.getParam(), paramArr,0);
            }
            if(value == null){
                value = buildValueOfBody(apiParams.getBody(),paramArr, 0);
            }
            if(value == null){
                value = buildValueOfCookie(apiParams.getCookie(),apiParams.getRequest(), paramArr,0);
            }
            if(value == null){
                value = buildValueOfHeader(apiParams.getHeader(),paramArr,0);
            }
            if(value == null){
                value = buildValueOfSession(apiParams.getSession(),paramArr,0);
            }
        }
        return value;
    }

    private Object buildValueOfScriptContent(Bindings bindings, String[] paramArr, int index) {
        if (bindings == null)return null;
        return buildObjectValue(bindings,paramArr,index,paramArr[index]);
    }

    public Object buildValueOfSession(Map<String,Object> session,String[] paramArr,int index) {
        if (session == null){
            return null;
        }
        return buildObjectValue(session,paramArr,index,paramArr[index]);
    }

    private Object buildValueOfHeader(Map<String,String> header,String[] paramArr,int index) {
        String varName = paramArr[index].toLowerCase();
        if (header == null){
            return null;
        }
        return buildObjectValue(header,paramArr,index,varName);
    }

    private Object buildValueOfCookie(Map<String,Object> cookie,HttpServletRequest request,  String[] paramArr,int index) {

        String varName = paramArr[index];
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

        if (paramArr.length-1 > index){
            return buildObjectValue(value,paramArr,index+1,paramArr[index + 1]);
        }
        return value;
    }

    private Object buildValueOfBody(Map<String,Object> body, String[] paramArr,int index) {
        if (body == null)return null;
        return buildObjectValue(body,paramArr,index,paramArr[index]);
    }

    private Object buildValueOfParameter(Map<String,Object> params, String[] paramArr,int index) {
        if (params == null)return null;
        return buildObjectValue(params,paramArr,index,paramArr[index]);
    }

    private Object buildObjectValue(Object inputParam,String[] paramArr,int index,String varName){

        if (inputParam == null){
            return null;
        }

        Map params = null;
        if (inputParam instanceof Map){
            params = (Map) inputParam;
        }else if(inputParam instanceof String){
            try {
                params = objectMapper.readValue(inputParam.toString(), Map.class);
            } catch (IOException e) {
                throw new IllegalArgumentException(inputParam+" Cannot be cast to Map.class");
            }
        }else{
            try {
                params = objectMapper.readValue(objectMapper.writeValueAsBytes(inputParam), Map.class);
            } catch (IOException e) {
                throw new IllegalArgumentException(inputParam+"Cannot be cast to Map.class");
            }
        }

        Object value = null;
        ArrVar arrVar = isArrVar(varName);
        if (arrVar != null){
            Object collection = params.get(arrVar.getVarName());
            if (collection == null){
                throw new IllegalArgumentException("The "+arrVar.getVarName()+" parameter is null");
            }
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

        if (paramArr.length-1 > index){
            return buildObjectValue(value,paramArr,index+1,paramArr[index+1]);
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

    private String buildValue(Object val,DataSourceDialect sourceDialect) {
        if (val == null)return null;
        StringBuilder valStr = new StringBuilder();
        if (val instanceof Collection){
            valStr.append(((Collection)val).stream().map(item->buildStrValue(item,sourceDialect)).collect(Collectors.joining(",")));
        }else {
            valStr.append(buildStrValue(val,sourceDialect));
        }
        return valStr.toString();
    }

    private String buildStrValue(Object val, DataSourceDialect sourceDialect){
        if (val == null)return null;
        if (val instanceof Number){
            return val.toString();
        }
        return "'"+transcoding(val.toString(),sourceDialect)+"'";
    }

    public String transcoding(String input, DataSourceDialect sourceDialect){
        if (sourceDialect == null){
            return input;
        }
        return sourceDialect.transcoding(input);
    }
}

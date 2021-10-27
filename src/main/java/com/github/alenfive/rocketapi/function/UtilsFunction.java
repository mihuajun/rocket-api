package com.github.alenfive.rocketapi.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiType;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiInfoCache;
import com.github.alenfive.rocketapi.extend.IScriptEncrypt;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import com.github.alenfive.rocketapi.utils.CsvUtils;
import com.github.alenfive.rocketapi.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 工具类
 */
@Component
public class UtilsFunction implements IFunction{

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ScriptParseService scriptParseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IApiInfoCache apiInfoCache;

    @Autowired
    private IScriptEncrypt scriptEncrypt;

    @Autowired
    @Lazy
    private IScriptParse scriptParse;

    @Override
    public String getVarName() {
        return "Utils";
    }

    /**
     * 获取请求域中的指定参数
     * @param varName
     */
    public Object val(String varName){
        return scriptParseService.buildRequestScopeParamItem(apiInfoContent.getApiParams(),null,varName);
    }

    /**
     * 允许接受一个默认值
     * @param varName
     * @param defaultValue
     * @return
     */
    public Object val(String varName,Object defaultValue){
        return Optional.ofNullable(val(varName)).orElse(defaultValue);
    }

    /**
     * 无返回结构体方法
     * @param data
     * @return
     */
    public IgnoreWrapper ignoreWrapper(Object data){
        return new IgnoreWrapper(data);
    }

    /**
     * 文件下载
     * @param fileName
     * @param inputStream
     */
    public ResponseEntity<InputStreamResource> download(String fileName, InputStream inputStream) throws IOException {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String prefix = fileName.substring(0,fileName.indexOf("."));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.parse("attachment; filename="+ URLEncoder.encode(prefix, "utf-8")+suffix));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
    }

    /**
     * 图片在线预览
     * @param inputStream
     * @return
     */
    public ResponseEntity<InputStreamResource> preview(InputStream inputStream) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        return new ResponseEntity(inputStreamResource, headers, HttpStatus.OK);
    }

    /**
     * 导出csv
     * @param fileName
     * @param title
     * @param list
     * @return
     * @throws IOException
     */
    public ResponseEntity<InputStreamResource> exportCsv(String fileName, Map<String,String> title, List<Map<String,Object>> list) throws IOException {
        ByteArrayInputStream inputStream = CsvUtils.writeCsv(title,list);
        return this.download(fileName+".csv",inputStream);
    }

    public ResponseEntity<InputStreamResource> exportCsv(String fileName, List<Map<String,Object>> list) throws IOException {
        Map<String,String> title = new LinkedHashMap<>();
        if (list.size() > 0){
            list.get(0).keySet().stream().forEach(item->title.put(item,item));
        }
        return exportCsv(fileName,title,list);
    }

    /**
     * 导出xls
     * @param fileName
     * @param title
     * @param list
     * @return
     * @throws IOException
     */
    public ResponseEntity<InputStreamResource> exportXls(String fileName, Map<String,String> title, List<Map<String,Object>> list) throws IOException {
        ByteArrayInputStream inputStream = ExcelUtils.writeXls(title,list);
        return this.download(fileName+".xls",inputStream);
    }

    public ResponseEntity<InputStreamResource> exportXls(String fileName, List<Map<String,Object>> list) throws IOException {
        Map<String,String> title = new LinkedHashMap<>();
        if (list.size() > 0){
            list.get(0).keySet().stream().forEach(item->title.put(item,item));
        }
        return exportXls(fileName,title,list);
    }

    /**
     * 解析XLS文件
     * @param in
     * @return
     */
    public List<Map<String,String>> parseXls(InputStream in) throws IOException {
        return ExcelUtils.parseXls(in);
    }

    /**
     * 解析CSV文件
     * @param in
     * @return
     */
    public List<Map<String,String>> parseCsv(List<String> titles,InputStream in) throws IOException {
        return CsvUtils.parseCsv(titles,in);
    }

    public List<Map<String,String>> parseCsv(InputStream in) throws IOException {
        return CsvUtils.parseCsv(in);
    }

    /**
     * 解析XLSX文件
     * @param in
     * @return
     */
    public List<Map<String,String>> parseXlsx(InputStream in) throws IOException {
        return ExcelUtils.parseXlsx(in);
    }

    /**
     * 导出xlsx
     * @param fileName
     * @param title
     * @param list
     */
    public ResponseEntity<InputStreamResource> exportXlsx(String fileName, Map<String,String> title, List<Map<String,Object>> list) throws IOException {
        ByteArrayInputStream inputStream = ExcelUtils.writeXlsx(title,list);
        return this.download(fileName+".xlsx",inputStream);
    }

    public ResponseEntity<InputStreamResource> exportXlsx(String fileName, List<Map<String,Object>> list) throws IOException {
        Map<String,String> title = new LinkedHashMap<>();
        if (list.size() > 0){
            list.get(0).keySet().stream().forEach(item->title.put(item,item));
        }
        return exportXlsx(fileName,title,list);
    }

    /**
     * json转Object
     * @param obj
     * @return
     * @throws JsonProcessingException
     */
    public String pasreToString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 字符串转Object
     * @param str
     * @return
     * @throws IOException
     */
    public Object pasreToObject(String str) throws IOException {
        return objectMapper.readValue(str,Object.class);
    }

    /**
     * 接口调用
     * @param target
     * @return
     */
    public Object loadAPI(String target) throws Throwable {
        if(StringUtils.isEmpty(target)){
            throw new IllegalArgumentException("parameter `target` is empty");
        }
        String[] targetArr = target.split(":");
        if (targetArr.length != 2){
            throw new IllegalArgumentException("parameter `target` needs to \"GET:/test/pager\"");
        }
        String method = targetArr[0];
        String fullPath = targetArr[1];
        ApiInfo apiInfo = apiInfoCache.get(ApiInfo.builder().method(method).fullPath(fullPath).build());

        if (apiInfo == null || !ApiType.Ql.name().equals(apiInfo.getType())){
            throw new IllegalArgumentException("API not found "+target);
        }

        StringBuilder script = new StringBuilder(scriptEncrypt.decrypt(apiInfo.getScript()));
        return scriptParse.engineEval(script.toString(),apiInfoContent.getEngineBindings());
    }
}

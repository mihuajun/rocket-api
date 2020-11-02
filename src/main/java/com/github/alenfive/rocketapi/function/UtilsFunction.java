package com.github.alenfive.rocketapi.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import com.github.alenfive.rocketapi.utils.CsvUtils;
import com.github.alenfive.rocketapi.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public String getVarName() {
        return "Utils";
    }

    /**
     * 获取上下文中的指定变量
     * @param varName
     */
    public Object val(String varName){
        return scriptParseService.buildParamItem(apiInfoContent.getApiParams(),varName);
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

    /**
     * 导出xlsx
     * @param fileName
     * @param title
     * @param list
     */
    public void exportXlsx(String fileName, LinkedHashMap title, List<Map<String,Object>> list){

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
     * 解析XLS文件
     * @param in
     * @return
     */
    public List<List<String>> parseXls(InputStream in) throws IOException {
        return ExcelUtils.parseXls(in);
    }

    /**
     * json转对象
     * @param jsonObj
     * @return
     * @throws JsonProcessingException
     */
    public String pasreJsonToString(Object jsonObj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(jsonObj);
    }

    /**
     * 字符串转json
     * @param jsonStr
     * @return
     * @throws IOException
     */
    public Object pasreStringToJson(String jsonStr) throws IOException {
        return objectMapper.readValue(jsonStr,Object.class);
    }
}

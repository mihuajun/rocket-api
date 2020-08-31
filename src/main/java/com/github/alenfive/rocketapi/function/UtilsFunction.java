package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import lombok.Cleanup;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    private HttpServletResponse response;

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        sb.append(title.values().stream().collect(Collectors.joining(", "))).append("\r\n");
        Set<String> titleKeys = title.keySet();
        for(Map<String,Object> info : list){
            for(String key : titleKeys){
                Object valObj = info.get(key);
                String valStr = null;
                if (valObj instanceof Date){
                    valStr = valObj == null?"":sdf.format(valObj);
                }else {
                    valStr = valObj == null?"":valObj.toString();
                }
                sb.append(valStr+"\t").append(",");
            }
            sb.append("\r\n");
        }
        return this.download(fileName+".csv",new ByteArrayInputStream(sb.toString().getBytes("utf-8")));
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

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        // 创建第一行,标题行
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell;
        //设置列名
        Object[] arrTitle = title.values().toArray();
        for (int i = 0; i < arrTitle.length; i++) {
            Object item = arrTitle[i];
            cell = row.createCell(i);
            cell.setCellValue(item.toString());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Set<String> titleKeys = title.keySet();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> info = list.get(i);
            row = sheet.createRow(i + 1);

            int col = 0;
            for(String key : titleKeys){
                Object valObj = info.get(key);
                String valStr = null;
                if (valObj instanceof Date){
                    valStr = valObj == null?"":sdf.format(valObj);
                }else {
                    valStr = valObj == null?"":valObj.toString();
                }

                cell = row.createCell(col ++);
                cell.setCellValue(valStr);
            }
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        wb.write(os);
        return this.download(fileName+".xls",new ByteArrayInputStream(os.toByteArray()));
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
        List<Map<String,String>> result = new ArrayList<>();
        String line = null;
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        while((line=reader.readLine())!=null){
            Map<String,String> item = new LinkedHashMap<>();
            List<String> itemData = Arrays.asList(line.split(","));
            for (int i=0;i<titles.size();i++){
                item.put(titles.get(i).trim(),itemData.get(i).trim());
            }
            result.add(item);
        }
        return result;
    }

    public List<Map<String,String>> parseCsv(InputStream in) throws IOException {
        List<Map<String,String>> result = new ArrayList<>();
        String line = null;
        List<String> titles = null;
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        boolean isFirst = true;
        while((line=reader.readLine())!=null){
            if (isFirst){
                titles = Arrays.asList(line.split(","));
                isFirst = false;
                continue;
            }
            Map<String,String> item = new LinkedHashMap<>();
            List<String> itemData = Arrays.asList(line.split(","));
            for (int i=0;i<titles.size();i++){
                item.put(titles.get(i).trim(),itemData.get(i).trim());
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 解析XLS文件
     * @param in
     * @return
     */
    public List<List<String>> parseXls(InputStream in) throws IOException {
        List<List<String>> result = new ArrayList<>();
        Workbook workbook = new HSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return result;
        }
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            List<String> item = new ArrayList<>(row.getLastCellNum());
            result.add(item);
            for (int k=0;k<row.getLastCellNum();k++){
                Cell cell = row.getCell(k);
                cell.setCellType(CellType.STRING);
                item.add(cell.getStringCellValue());
            }
        }
        return result;
    }
}

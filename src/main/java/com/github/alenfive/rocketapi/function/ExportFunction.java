package com.github.alenfive.rocketapi.function;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:导出
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
 */
@SuppressWarnings("DuplicatedCode")
@Component
public class ExportFunction implements IFunction {

    @Override
    public String getVarName() {
        return "Export";
    }

    @Autowired
    private HttpServletResponse response;

    public void csv(String fileName,Map<String,String> title, List<Map<String,Object>> list) throws IOException {

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8")+ ".csv");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Writer writer = response.getWriter();
        writer.append(title.values().stream().collect(Collectors.joining(", "))).append("\r\n");
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
                writer.append(valStr+"\t").append(",");
            }
            writer.append("\r\n");
        }
        writer.close();
    }

    public void xls(String fileName,Map<String,String> title, List<Map<String,Object>> list) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xls");

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

        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.close();

    }

    public void xlsx(String fileName,LinkedHashMap title, List<Map<String,Object>> list){

    }
}

package com.github.alenfive.rocketapi.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * excel处理
 */
public class ExcelUtils {

    public static ByteArrayInputStream writeXls(Map<String,String> title, List<Map<String,Object>> list) throws IOException {
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
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static List<List<String>> parseXls(InputStream in) throws IOException {
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

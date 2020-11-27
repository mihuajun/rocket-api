package com.github.alenfive.rocketapi.utils;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

    public static List<Map<String,String>> parseXls(InputStream in) throws IOException {
        List<Map<String,String>> result = new ArrayList<>();
        HSSFWorkbook workbook = new HSSFWorkbook(in);
        HSSFSheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return result;
        }
        List<String> titles = null;
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            HSSFRow row = sheet.getRow(i);

            if (i == 0){
                titles = new ArrayList<>(row.getLastCellNum());
                for (int k=0;k<row.getLastCellNum();k++){
                    HSSFCell cell = row.getCell(k);
                    cell.setCellType(CellType.STRING);
                    titles.add(cell.getStringCellValue().trim());
                }
                continue;
            }
            Map<String,String> item = new LinkedHashMap(row.getLastCellNum());
            result.add(item);
            for (int k=0;k<row.getLastCellNum();k++){
                HSSFCell cell = row.getCell(k);
                cell.setCellType(CellType.STRING);
                item.put(titles.get(k),cell.getStringCellValue().trim());
            }
        }
        return result;
    }


    public static ByteArrayInputStream writeXlsx(Map<String,String> title, List<Map<String,Object>> list) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        // 创建第一行,标题行
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell;
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

    public static List<Map<String,String>> parseXlsx(InputStream in) throws IOException {
        List<Map<String,String>> result = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        XSSFSheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return result;
        }
        List<String> titles = null;
        for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
            XSSFRow row = sheet.getRow(i);

            if (i == 0){
                titles = new ArrayList<>(row.getLastCellNum());
                for (int k=0;k<row.getLastCellNum();k++){
                    XSSFCell cell = row.getCell(k);
                    cell.setCellType(CellType.STRING);
                    titles.add(cell.getStringCellValue().trim());
                }
                continue;
            }
            Map<String,String> item = new LinkedHashMap(row.getLastCellNum());
            result.add(item);
            for (int k=0;k<row.getLastCellNum();k++){
                XSSFCell cell = row.getCell(k);
                cell.setCellType(CellType.STRING);
                item.put(titles.get(k),cell.getStringCellValue().trim());
            }
        }
        return result;
    }
}

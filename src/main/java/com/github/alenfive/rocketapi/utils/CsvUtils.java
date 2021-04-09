package com.github.alenfive.rocketapi.utils;

import lombok.Cleanup;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * csv 处理
 */
public class CsvUtils {
    public static ByteArrayInputStream writeCsv(Map<String, String> title, List<Map<String, Object>> list) throws UnsupportedEncodingException {
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
                valStr = valStr.replaceAll("\"","\\\\\"").replaceAll(",","\\\\,");
                sb.append(valStr+"\t").append(",");
            }
            sb.append("\r\n");
        }

        return new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
    }

    public static List<Map<String, String>> parseCsv(InputStream in) throws IOException {
        List<Map<String,String>> result = new ArrayList<>();
        String line = null;
        List<String> titles = null;
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        boolean isFirst = true;
        while((line=reader.readLine())!=null){
            if (isFirst){
                titles = Arrays.asList(line.split(",")).stream().map(item->item.trim()).collect(Collectors.toList());
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

    public static List<Map<String, String>> parseCsv(List<String> titles, InputStream in) throws IOException {
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
}

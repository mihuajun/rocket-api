package com.github.alenfive.rocketapi.utils;


import com.github.alenfive.rocketapi.annotation.ApiUpdateField;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FieldUtils {

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static List<String> allFields(Class clazz){
        return Arrays.stream(clazz.getDeclaredFields()).map(item->humpToLine2(item.getName())).collect(Collectors.toList());
    }

    public static List<String> updateFields(Class clazz){
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(item->item.getAnnotation(ApiUpdateField.class) != null)
                .map(item-> humpToLine2(item.getName())).collect(Collectors.toList());
    }

    /**
     * 下划线转驼峰
     */
    public static String underlineToCamel(String name){
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                if (++i < name.length()){
                    sb.append(Character.toUpperCase(name.charAt(i)));
                }
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰转下划线
     */
    public static String humpToLine2(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

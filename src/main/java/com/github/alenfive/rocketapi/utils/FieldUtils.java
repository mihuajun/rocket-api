package com.github.alenfive.rocketapi.utils;


import com.github.alenfive.rocketapi.annotation.ApiUpdateField;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FieldUtils {

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    public static List<String> allTableFields(Class clazz){
        return Arrays.stream(clazz.getDeclaredFields()).map(item->humpToLine2(item.getName())).collect(Collectors.toList());
    }

    public static List<String> allNameParamsFields(Class clazz){
        return Arrays.stream(clazz.getDeclaredFields()).map(item->":"+item.getName()).collect(Collectors.toList());
    }

    public static List<String> updateFields(Class clazz){
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(item->item.getAnnotation(ApiUpdateField.class) != null)
                .map(item-> humpToLine2(item.getName())).collect(Collectors.toList());
    }

    /**
     * 所有字母大写时toLower(),
     * 所有字母小写时启用驼峰转换，
     * 大小写字母都有，不转换
     * 下划线转驼峰
     */
    public static String underlineToCamel(String name){
        boolean allUpper = true;
        boolean allLower = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(Character.isLowerCase(c)){
                allUpper = false;
            }else if (Character.isUpperCase(c)){
                allLower = false;
            }
        }

        if (allUpper){
            name = name.toLowerCase();
            allLower = true;
        }
        if (!allLower){
            return name;
        }

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

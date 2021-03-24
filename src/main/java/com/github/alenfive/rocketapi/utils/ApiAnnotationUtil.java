package com.github.alenfive.rocketapi.utils;

import com.github.alenfive.rocketapi.annotation.ApiId;
import com.github.alenfive.rocketapi.annotation.ApiTable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2021/1/6 14:26
 * @UpdateDate: 2021/1/6 14:26
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
public class ApiAnnotationUtil {
    public static String getApiTableName(Class clazz){
        ApiTable apiTable = (ApiTable) clazz.getAnnotation(ApiTable.class);
        if (apiTable == null)throw new IllegalArgumentException("not found @ApiTable Annotation");
        String tableName = apiTable.value();
        if (StringUtils.isEmpty(tableName))throw new IllegalArgumentException("not found tableName for @ApiTable Annotation");
        return tableName;
    }

    public static String getApiIdFieldName(Class clazz) {
        String fieldName = Arrays.asList(clazz.getSuperclass().getDeclaredFields()).stream()
                .filter(item->item.getAnnotation(ApiId.class) != null).findFirst()
                .orElseThrow(()-> new IllegalArgumentException("not found @ApiId Annotation")).getName();
        return fieldName;
    }
}

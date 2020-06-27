package com.org.peysen.redisarticle.common;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author : mengmeng.pei
 * @Date : 2020/6/24
 * @Desc :
 */
public class Common {

    public static long getCurrentSeconds(){
        return System.currentTimeMillis() / 1000;
    }

    public static Map<String, Object> objectToMap(Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            // 获取原来的访问控制权限
            boolean accessFlag = field.isAccessible();
            // 修改访问控制权限
            field.setAccessible(true);
            // 获取属性名及其属性值
            String fieldName = field.getName();
            Object value = field.get(object);
            // 恢复访问控制权限
            field.setAccessible(accessFlag);

            if (null != value && StringUtils.isNotBlank(value.toString())) {
                map.put(fieldName, value);
            }
        }
        return map;
    }
}

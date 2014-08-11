package com.whosbean.qpush.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * gson的工具类
 *
 */
public class GsonUtils {

    public static <T> List<T> asList(String json, final T[] itemType)throws Exception {
        Gson gson = new Gson();
        T[] temp = (T[])gson.fromJson(json, itemType.getClass());
        return Arrays.asList(temp);
    }

    @SuppressWarnings("unchecked")
    public static <T>T asT(Class<?> clzz, String json) {
        if (json == null) return null;
        Gson gson = new GsonBuilder().create();
        return (T) gson.fromJson(json, clzz);
    }

    /**
     * 将一个对象转成json字符串
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        //创建GsonBuilder
        GsonBuilder builder = new GsonBuilder();
        //创建Gson并进行转换
        Gson gson = builder.create();
        return gson.toJson(obj);
    }

    public static Map<String, Object> convertJson2Map(String json) {
        if (json == null) return null;
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, HashMap.class);
    }

}

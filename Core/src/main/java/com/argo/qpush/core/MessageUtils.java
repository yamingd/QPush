package com.argo.qpush.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message序列化的工具类
 *
 */
public class MessageUtils {

    private static final MessagePack messagePack = new MessagePack();

    /**
     * 序列为MessagePack
     * @param obj
     * @return byte[]
     * @throws IOException
     */
    public static byte[] asBytes(Object obj) throws IOException {
        return messagePack.write(obj);
    }

    /**
     * 反序列化MessagePack
     * @param clazz
     * @param bytes
     * @param <T>
     * @return T
     * @throws IOException
     */
    public static <T> T asT(Class<?> clazz, byte[] bytes) throws IOException {
        T o = (T) messagePack.read(bytes, clazz);
        return o;
    }

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
     * @return String
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

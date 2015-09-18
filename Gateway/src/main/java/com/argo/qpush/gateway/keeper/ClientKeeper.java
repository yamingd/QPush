package com.argo.qpush.gateway.keeper;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维持产品客户端列表
 * Created by yaming_deng on 14-8-8.
 */
public class ClientKeeper {

    private static ConcurrentHashMap<Integer, Integer> mapping = new ConcurrentHashMap<Integer, Integer>();

    public static void init(){

    }

    /**
     * 注册一个产品
     * @param key
     */
    public static void registry(String key){

    }

    /**
     * 取消注册产品
     * @param key
     */
    public static void unregistry(String key){

    }

    /**
     * 添加一个客户端链接
     * @param key
     * @param token
     * @param channelId
     */
    public static Integer add(String key, String token, Integer channelId){
        Integer id = getId(key, token);
        mapping.put(id, channelId);
        return id;
    }

    private static int getId(String key, String token) {
        return String.format("%s:%s", key, token).hashCode();
    }

    /**
     * 移除一个客户端连接
     * @param key
     * @param token
     * @return Integer
     */
    public static Integer remove(String key, String token){
        Integer id = getId(key, token);
        Integer val = mapping.remove(id);
        return val;
    }

    /**
     * 取得一个客户端连接标示
     * @param key
     * @param token
     * @return Integer
     */
    public static Integer get(String key, String token){
        Integer id = getId(key, token);
        return mapping.get(id);
    }

    public static Integer count(String key){
        return mapping.size();
    }

    public static Collection<Integer> getAll(String appKey) {
        return mapping.values();
    }
}

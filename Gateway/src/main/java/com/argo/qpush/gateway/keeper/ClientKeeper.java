package com.argo.qpush.gateway.keeper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维持产品客户端列表
 * Created by yaming_deng on 14-8-8.
 */
public class ClientKeeper {

    private static Map<String, ConcurrentHashMap<String, Integer>> mapping = new HashMap<String, ConcurrentHashMap<String, Integer>>();

    public static void init(){

    }

    /**
     * 注册一个产品
     * @param key
     */
    public static void registry(String key){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m == null){
            m = new ConcurrentHashMap<String, Integer>();
            mapping.put(key, m);
        }
    }

    /**
     * 取消注册产品
     * @param key
     */
    public static void unregistry(String key){
        mapping.remove(key);
    }

    /**
     * 添加一个客户端链接
     * @param key
     * @param token
     * @param channelId
     */
    public static void add(String key, String token, Integer channelId){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m != null){
            m.put(token, channelId);
        }
    }

    /**
     * 移除一个客户端连接
     * @param key
     * @param token
     * @return
     */
    public static Integer remove(String key, String token){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m != null){
            Integer val = m.remove(token);
            return val;
        }
        return null;
    }

    /**
     * 取得一个客户端连接标示
     * @param key
     * @param token
     * @return
     */
    public static Integer get(String key, String token){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m != null){
            return m.get(token);
        }
        return null;
    }

    /**
     * 获取某产品的所有客户端链接标示
     * @param key
     * @return
     */
    public static Collection<Integer> gets(String key){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m != null){
            return m.values();
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * 计算某产品的客户端链接总数
     * @param key
     * @return
     */
    public static Integer count(String key){
        ConcurrentHashMap<String, Integer> m = mapping.get(key);
        if(m != null){
            return m.values().size();
        }
        return 0;
    }
}

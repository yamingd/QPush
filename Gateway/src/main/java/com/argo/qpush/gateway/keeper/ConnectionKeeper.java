package com.argo.qpush.gateway.keeper;

import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.ServerMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维持长链接.
 * Created by yaming_deng on 14-8-8.
 */
public class ConnectionKeeper {

    protected static Logger logger = LoggerFactory.getLogger(ConnectionKeeper.class);

    /**
     * 连接池
     */
    private static ConcurrentHashMap<Integer, Connection> pools = new ConcurrentHashMap<Integer, Connection>();

    public static void init() {
        // 初始化所有app mapping
        ServerMetrics.log();
        ClientKeeper.init();
    }

    /**
     * 移除所有链接.
     *
     * @param productId
     */
    public static void removeAll(String productId) {
        int t = 0;
        Collection<Integer> cs = ClientKeeper.gets(productId);
        for (Integer cid : cs){
            Connection c = pools.remove(cid);
            if (c != null) {
                c.close();
                t++;
            }
        }
        if (t > 0) {
            ServerMetrics.updateConnection(-1 * t);
            ClientKeeper.unregistry(productId);
        }
    }

    /**
     * 添加连接
     *
     * @param productId:   Product Id
     * @param token
     * @param conn
     */
    public static boolean add(String productId, String token, Connection conn) {
        Integer cid = ClientKeeper.get(productId, token);
        if ( cid != null){
            //关闭旧连接.
            Connection c = pools.remove(cid);
            if (c != null) {
                ServerMetrics.decrConnection();
            }
        }

        cid = conn.getContext().channel().hashCode();
        ClientKeeper.add(productId, token, cid);
        pools.put(cid, conn);

        ServerMetrics.incrConnection();

        return true;
    }

    /**
     * 获取连接
     *
     * @param token
     * @return
     */
    public static Connection get(String productId, String token) {
        Integer cid = ClientKeeper.get(productId, token);
        if(cid != null) {
            return pools.get(cid);
        }
        return null;
    }

    public static Connection get(int channelId) {
        return pools.get(channelId);
    }

    /**
     * 移除连接
     *
     * @param productId
     * @param token
     */
    public static void remove(String productId, String token) {
        Integer cid = ClientKeeper.remove(productId, token);
        if(cid != null){
            Connection c = pools.remove(cid);
            if (c == null) {
                return;
            }
            ServerMetrics.decrConnection();
        }
    }

    public static void remove(Integer channelId) {
        Connection c = pools.remove(channelId);
        if (c == null) {
            return;
        }
        ServerMetrics.decrConnection();
    }
}

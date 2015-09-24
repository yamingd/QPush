package com.argo.qpush.client;

import io.netty.channel.nio.NioEventLoopGroup;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientProxyDelegate {

    protected static Logger logger = LoggerFactory.getLogger(ClientProxyDelegate.class);

    private static Properties props = new Properties();
    public static MessagePack messagePack = new MessagePack();
    public static ClientProxyDelegate instance = new ClientProxyDelegate();

    private List<ClientConnection> connections = new ArrayList<ClientConnection>();

    private NioEventLoopGroup workerGroup;
    private int concurrentConnectionMax = 1;
    private volatile boolean stopping = false;

    static {

        messagePack.register(AppPayload.class);

        loadConfig();

    }

    private static void loadConfig() {
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qpush_client.properties"));
        } catch (FileNotFoundException e) {
            props = null;
            logger.error("配置文件未找到", e);
        } catch (IOException e) {
            props = null;
            logger.error("配置文件加载失败", e);
        }
        if (props == null){
            throw new RuntimeException("qpush_client.properties not found.");
        }
    }

    public synchronized void start() {

        if (props == null){
            loadConfig();
            if (props == null){
                throw new RuntimeException("qpush_client.properties not found.");
            }
        }

        concurrentConnectionMax = Integer.parseInt(props.getProperty("concurrent_max", "1")) + 1;
        // Never use more threads than concurrent connections (Netty binds a channel to a single thread, so the
        // excess threads would always go unused)
        final int threadCount = Math.min(concurrentConnectionMax, Runtime.getRuntime().availableProcessors() * 2);
        this.workerGroup = new NioEventLoopGroup(threadCount);
        // Start the client.
        for(int i=0; i<concurrentConnectionMax; i++){
            newConnection();
        }
    }

    /**
     * 建立链接
     */
    public void newConnection(){
        synchronized (connections){
            ClientConnection connection = new ClientConnection(props, this.workerGroup);
            connection.connect();
            connections.add(connection);
        }
    }

    /**
     * 移除链接
     * @param connection
     */
    public void removeConnection(ClientConnection connection){
        synchronized (connections){
            final boolean flag = connections.remove(connection);
            assert flag;
        }
    }

    /**
     * 获取一个可用的链接
     * @param task
     */
    public void get(final ChannelAvailable task){
        ClientConnection clientConnection = connections.get(0);
        //TODO: check connections is Empty
        task.execute(clientConnection);
    }

    public void close(){
        stopping = true;

        synchronized (this.connections) {
            for (final ClientConnection connection : this.connections) {
                connection.shutdown();
            }
        }

        try {
            workerGroup.shutdownGracefully().await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isStopped(){
        return stopping;
    }

}

package com.argo.qpush.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientProxyDelegate {

    protected static Logger logger = LoggerFactory.getLogger(ClientProxyDelegate.class);

    private static Properties props = new Properties();
    public static MessagePack messagePack = new MessagePack();
    public static ClientProxyDelegate instance = new ClientProxyDelegate();

    private ConcurrentLinkedDeque<ChannelHandlerContext> channelList = new ConcurrentLinkedDeque<ChannelHandlerContext>();
    private final Bootstrap b = new Bootstrap(); // (1)
    private NioEventLoopGroup workerGroup;
    private String host;
    private Integer port;
    private volatile boolean stopping = false;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

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

    public void start() {
        connect();
    }

    private void connect(){

        if (props == null){
            loadConfig();
            if (props == null){
                throw new RuntimeException("qpush_client.properties not found.");
            }
        }

        port = Integer.parseInt(props.getProperty("port", "8081"));
        host = props.getProperty("host", "127.0.0.1");
        logger.info("QPush server. connecting... host=" + host + "/" + port);

        workerGroup = new NioEventLoopGroup();
        try {
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {

                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("bytesDecoder",new ByteArrayDecoder());

                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                    pipeline.addLast("bytesEncoder", new ByteArrayEncoder());

                    pipeline.addLast("handler", new ClientConnectHandler());
                }
            });

            // Start the client.
            for(int i=0; i<workerGroup.executorCount() * 2; i++){
                ChannelFuture f = newChannel();
                if (f!=null) {
                    try {
                        f.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
             }

            countDownLatch.countDown();

            logger.info("QPush server. connected. {}:{}", host, port);

        } catch (Exception e){
            logger.error("QPush server connect error.", e);
            workerGroup.shutdownGracefully();
        }
    }

    public ChannelFuture newChannel(){
        ChannelFuture f = b.connect(host, port); // (5)
        if(f.cause() != null){
            logger.error("QPush newChannel.", f.cause());
            return null;
        }
        return f;
    }

    public void save(ChannelHandlerContext c){
        channelList.add(c);
    }

    public void get(final ChannelAvailable task){
        ChannelHandlerContext c = channelList.pop();
        if (c == null){
            logger.error("No ChannelAvailable");
            return;
        }
        channelList.add(c);
        task.execute(c);
    }

    public void remove(ChannelHandlerContext c){
        channelList.remove(c);
    }

    public void close(){
        stopping = true;
        workerGroup.shutdownGracefully();
    }

    public boolean isStopped(){
        return stopping;
    }

}

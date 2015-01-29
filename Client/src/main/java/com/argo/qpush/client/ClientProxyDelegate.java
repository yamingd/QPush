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
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListSet;
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

    private ConcurrentSkipListSet<Channel> channelList = new ConcurrentSkipListSet<Channel>();
    private final Bootstrap b = new Bootstrap(); // (1)
    private NioEventLoopGroup workerGroup;
    private String host;
    private Integer port;
    private volatile boolean stopping = false;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    static{

        messagePack.register(AppPayload.class);

        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qpush_client.properties"));
        } catch (FileNotFoundException e) {
            logger.error("配置文件未找到", e);
        } catch (IOException e) {
            logger.error("配置文件加载失败", e);
        }

    }

    public void start() {
        connect();
    }

    private void connect(){
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
            for(int i=0; i<workerGroup.executorCount(); i++){
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

    public void save(Channel c){
        channelList.add(c);
    }

    public void get(final ChannelAvaliable task){
        Channel c = channelList.pollFirst();
        if (c == null){
            final ChannelFuture f = newChannel();
            if (f == null){
                return;
            }

            f.addListener(new GenericFutureListener<Future<? super java.lang.Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.cause() == null){
                        final Channel c0 = f.channel();
                        task.execute(c0);
                    }else{
                        logger.error("QPush newChannel.", f.cause());
                    }
                }
            });

        }else{
            task.execute(c);
            channelList.add(c);
        }
    }

    public void remove(Channel c){
        channelList.remove(c);
    }

    public void close(){
        stopping = true;
        workerGroup.shutdownGracefully();
    }

    public boolean isStopped(){
        return stopping;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown Hook is running !");
                ClientProxyDelegate.instance.close();
            }
        });
    }
}

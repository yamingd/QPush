package com.whosbean.qpush.client;

import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientProxyDelegate {

    protected static Logger logger = LoggerFactory.getLogger(ClientProxyDelegate.class);

    private static Properties props = new Properties();
    public static MessagePack messagePack = new MessagePack();
    public static ClientProxyDelegate instance = new ClientProxyDelegate();

    private List<Channel> channelList = Lists.newArrayList();
    private AtomicInteger seq = new AtomicInteger();
    private final Bootstrap b = new Bootstrap(); // (1)
    private EventLoopGroup workerGroup;
    private String host;
    private Integer port;
    private volatile boolean stopping = false;

    static{

        messagePack.register(AppPayload.class);

        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qpush_client.properties"));
        } catch (FileNotFoundException e) {
            logger.error("配置文件未找到", e);
        } catch (IOException e) {
            logger.error("配置文件加载失败", e);
        }

        ClientProxyDelegate.instance.start();
    }

    public void start(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        });

        thread.start();

        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void connect(){
        port = Integer.parseInt(props.getProperty("port", "8081"));
        final int pool = Integer.parseInt(props.getProperty("thread_pool", "10"));
        host = props.getProperty("host", "127.0.0.1");

        logger.info("QPush server. connecting... host=" + host + "/" + port);

        workerGroup = new NioEventLoopGroup(pool);
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

            final List<ChannelFuture> fs = new ArrayList<ChannelFuture>();
            // Start the client.
            for(int i=0; i<pool; i++){
                ChannelFuture f = newChannel();
                if (f!=null) {
                    fs.add(f);
                }
             }

             for (ChannelFuture f : fs){
                 if (!f.isDone()) {
                     f.get();
                 }
             }

             System.out.println("QPush server. connected.");

        } catch (Exception e){
            logger.error("QPush server connect error.", e);
            workerGroup.shutdownGracefully();
        }
    }

    public ChannelFuture newChannel(){
        ChannelFuture f = b.connect(host, port); // (5)
        if(f.cause() != null){
            f.cause().printStackTrace();
            return null;
        }
        return f;
    }

    public void save(Channel c){
        channelList.add(c);
    }

    public Channel get(){
        long id = seq.getAndIncrement();
        id = id % channelList.size();
        Channel c = channelList.get((int)id);
        return c;
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

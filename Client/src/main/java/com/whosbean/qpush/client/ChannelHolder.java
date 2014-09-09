package com.whosbean.qpush.client;

import com.google.common.collect.Lists;
import com.whosbean.qpush.core.entity.Payload;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
public class ChannelHolder {

    protected static Logger logger = LoggerFactory.getLogger(ChannelHolder.class);

    private static Properties props = new Properties();
    private static List<Channel> channelList = Lists.newArrayList();
    private static AtomicInteger seq = new AtomicInteger();
    private static final Bootstrap b = new Bootstrap(); // (1)
    private static EventLoopGroup workerGroup;
    private static MessagePack messagePack = new MessagePack();

    static{
        try {
            props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("qpush_client.properties"));
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("connecting to QPush server.");
                    connect();
                }
            });
            thread.start();

        } catch (FileNotFoundException e) {
            logger.error("配置文件未找到", e);
        } catch (IOException e) {
            logger.error("配置文件加载失败", e);
        }
    }

    private static void connect(){
        final int port = Integer.parseInt(props.getProperty("port", "8081"));
        final int pool = Integer.parseInt(props.getProperty("thread_pool", "10"));
        final String host = props.getProperty("host", "127.0.0.1");
        workerGroup = new NioEventLoopGroup(pool);
        try {
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.option(ChannelOption.TCP_NODELAY, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ClientConnectHandler());
                }
            });

            final List<ChannelFuture> fs = new ArrayList<ChannelFuture>();
            // Start the client.
            for(int i=0; i<pool; i++){
                ChannelFuture f = b.connect(host, port); // (5)
                if(f.cause() != null){
                    f.cause().printStackTrace();
                    continue;
                }
                fs.add(f);
             }

            for(int i=0; i<fs.size(); i++){
                ChannelFuture f = fs.get(i); // (5)
                if(f.isDone()){

                }
            }

        } catch (Exception e){
            e.printStackTrace();
            workerGroup.shutdownGracefully();
        }
    }

    public static void save(Channel c){
        channelList.add(c);
    }

    public static Channel get(){
        long id = seq.getAndIncrement();
        id = id % channelList.size();
        Channel c = channelList.get((int)id);
        return c;
    }

    public static void remove(Channel c){
        channelList.remove(c);
    }

    public static boolean send(Payload payload) throws IOException {
        Channel c = get();
        byte[] bytes = toBytes(payload);
        final ByteBuf data = c.config().getAllocator().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        ChannelFuture cf = c.writeAndFlush(data);
        if(cf.isDone()){
            if(cf.cause() != null){
                cf.cause().printStackTrace();
                c.close();
                channelList.remove(c);
                return false;
            }
            return true;
        }
        return false;
    }

    public static byte[] toBytes(Object obj) throws IOException {
        return messagePack.write(obj);
    }

    public static void close(){
        workerGroup.shutdownGracefully();
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Shutdown Hook is running !");
                close();
            }
        });
    }
}

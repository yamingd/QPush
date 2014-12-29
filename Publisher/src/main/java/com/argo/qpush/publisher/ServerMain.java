package com.argo.qpush.publisher;

import com.argo.qpush.publisher.handler.PublisherConnHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Properties;

/**
 * 消息发布者服务
 * Created by yaming_deng on 14-8-8.
 */
public class ServerMain {

    private static int port = 8080;

    /**
     * 启动推送服务 8080端口
     */
    public static void start(Properties prop) {
        port = Integer.parseInt(prop.getProperty("server.port", "8180"));

        EventLoopGroup parentGroup = new NioEventLoopGroup(); // 用于接收发来的连接请求
        EventLoopGroup childGroup = new NioEventLoopGroup(); // 用于处理parentGroup接收并注册给child的连接中的信息

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap(); // 服务器助手类
            // 简历新的accept连接，用于构建serverSocketChannel的工厂类
            serverBootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast("bytesDecoder",new ByteArrayDecoder());

                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                            pipeline.addLast("bytesEncoder", new ByteArrayEncoder());

                            pipeline.addLast("handler", new PublisherConnHandler());
                        }
                    });

            serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            serverBootstrap.option(ChannelOption.TCP_NODELAY, true);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);

            System.out.println("start server " + port + " ... ");
            ChannelFuture f = serverBootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            childGroup.shutdownGracefully();
            parentGroup.shutdownGracefully();
        }
    }

    /**
     * 推送服务入口
     * @param args
     */
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-publisher.xml");
        Properties prop = context.getBean("appConfig", Properties.class);
        start(prop);
    }

}

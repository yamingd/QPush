package com.argo.qpush.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by yamingd on 9/24/15.
 */
public class ClientConnection {

    protected static Logger logger = LoggerFactory.getLogger(ClientConnection.class);

    private Properties config;
    private NioEventLoopGroup nioEventLoopGroup;
    private String host;
    private Integer port;
    private ChannelFuture connectFuture;
    private Bootstrap bootstrap;

    public ClientConnection(Properties config, NioEventLoopGroup loopGroup) {
        this.config = config;
        this.nioEventLoopGroup = loopGroup;

        port = Integer.parseInt(config.getProperty("port", "8081"));
        host = config.getProperty("host", "127.0.0.1");

    }

    /**
     *
     */
    public synchronized void connect(){

        bootstrap = new Bootstrap();
        final ClientConnection clientConnection = this;

        bootstrap.group(this.nioEventLoopGroup); // (2)
        bootstrap.channel(NioSocketChannel.class); // (3)
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.AUTO_CLOSE, false);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {

                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("bytesDecoder", new ByteArrayDecoder());

                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4, false));
                pipeline.addLast("bytesEncoder", new ByteArrayEncoder());

                pipeline.addLast("handler", new ClientConnectHandler(clientConnection));
            }
        });

        logger.info("QPush server. connecting... {}", this);
        doConnect(false);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClientConnection{");
        sb.append("host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }

    private void doConnect(boolean reconnect) {
        final ClientConnection clientConnection = this;
        this.connectFuture = bootstrap.connect(this.host, this.port);
        this.connectFuture.addListener(new GenericFutureListener<ChannelFuture>() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()){
                    if (!reconnect) {
                        logger.error("Connect Error.", future.cause());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }

                    logger.info("Reconnect QPush Server. {}", clientConnection);
                    doConnect(true);

                }else{
                    logger.info("Connect Success. {}", clientConnection);
                }
            }
        });
    }

    /**
     *
     */
    public synchronized void reconnect(){
        doConnect(true);
    }

    /**
     * 发送消息
     * @param bytes 数据
     * @param listener 发送回调
     */
    public void send(final byte[] bytes, final GenericFutureListener<? extends Future<? super Void>> listener){

        final ClientConnection clientConnection = this;

        this.connectFuture.channel().eventLoop().execute(new Runnable() {

            @Override
            public void run() {

                clientConnection.connectFuture.channel().writeAndFlush(bytes).addListener(listener);

            }
        });
    }

    /**
     * 停止
     */
    public void shutdown(){
        if (connectFuture.isCancellable()){
            connectFuture.cancel(true);
        }else{
            connectFuture.channel().close();
        }
    }
}

package com.whosbean.qpush.gateway;

import com.whosbean.qpush.core.GsonUtils;
import com.whosbean.qpush.core.entity.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class Connection {

    protected static Logger logger = LoggerFactory.getLogger(Connection.class);

    private final Channel channel;

    public Connection(Channel channel) {
        this.channel = channel;
    }

    public boolean send(Payload message) throws Exception {
        // 组装消息包
        if(channel.isOpen()){
            Map<String, Object> map = new HashMap<String, Object>();
            Map<String, Object> aps = new HashMap<String, Object>();
            aps.put("alert", message.getTitle());
            aps.put("badge", message.getBadge());
            aps.put("sound", message.getSound());
            map.put("aps", aps);
            map.put("userInfo", GsonUtils.asT(Map.class, message.getExtras()));
            byte[] msg = GsonUtils.toJson(map).getBytes("UTF-8");
            return send(msg);
        }else{
            logger.error("Send Error. Channel is closed.");
        }
        return false;
    }

    public boolean send(byte[] msg) {
        final ByteBuf data = channel.config().getAllocator().buffer(msg.length); // (2)
        data.writeBytes(msg);
        ChannelFuture cf = channel.writeAndFlush(data);
        if(cf.isDone() && cf.cause() != null){
            ServerMetrics.incrPushTotal(false);
            logger.error("Send Error.", cf.cause());
            return false;
        }
        long total = ServerMetrics.incrPushTotal(true);
        logger.error("Send OK. totalPush=" + total);
        return true;
    }

    public void close(){
        channel.close();
    }

    public Channel getChannel() {
        return channel;
    }

}

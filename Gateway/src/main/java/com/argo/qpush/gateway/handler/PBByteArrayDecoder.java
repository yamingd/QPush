package com.argo.qpush.gateway.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by user on 1/22/15.
 */
public class PBByteArrayDecoder extends ByteArrayDecoder {

    protected static Logger logger = LoggerFactory.getLogger(PBByteArrayDecoder.class);


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        super.decode(ctx, msg, out);
        if (logger.isDebugEnabled()){
            logger.debug("ByteArray: {}", out.size());
        }
    }
}

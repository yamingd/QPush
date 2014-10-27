package com.whosbean.qpush.client;


import io.netty.channel.Channel;

/**
 * Created by Yaming on 2014/10/27.
 */
public interface ChannelAvaliable {

    /**
     *
     * @param channel
     */
    void execute(final Channel channel);

}

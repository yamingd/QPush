package com.argo.qpush.client;

import org.msgpack.annotation.MessagePackMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yamingd on 8/6/15.
 */
@MessagePackMessage
public class TopicMessage {

    /**
     * 业务数据库的唯一标示
     */
    public long objectId;
    /**
     * Topic名称
     */
    public String title;

    /**
     * 包含的用户标示
     */
    public List<String> clientIds = new ArrayList<String>();

}

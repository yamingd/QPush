package com.whosbean.qpush.apns;

import com.whosbean.qpush.core.MessageUtils;
import com.whosbean.qpush.core.entity.Payload;
import org.msgpack.annotation.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * For IOS.
 * Created by yaming_deng on 14-9-10.
 */
@Message
public class APNSMessage {

    public Map<String, String> userInfo;
    public APSBody aps;

    public APNSMessage() {
        aps = new APSBody();
        userInfo = new HashMap<String, String>();
    }

    public APNSMessage(Payload data){
        userInfo = MessageUtils.asT(Map.class, data.getExtras());
        aps = new APSBody();
        aps.alert = data.getTitle();
        aps.badge = data.getBadge();
        aps.sound = data.getSound();
    }

    @Override
    public String toString() {
        return "APNSMessage{" +
                "userInfo=" + userInfo +
                ", aps=" + aps +
                '}';
    }
}

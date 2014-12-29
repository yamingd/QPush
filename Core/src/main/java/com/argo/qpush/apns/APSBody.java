package com.argo.qpush.apns;

import org.msgpack.annotation.Message;

/**
 * Created by zhza4586 on 14-9-10.
 */
@Message
public class APSBody{

    public String alert;
    public String sound;
    public Integer badge;

    @Override
    public String toString() {
        return "APSBody{" +
                "alert='" + alert + '\'' +
                ", sound='" + sound + '\'' +
                ", badge=" + badge +
                '}';
    }
}

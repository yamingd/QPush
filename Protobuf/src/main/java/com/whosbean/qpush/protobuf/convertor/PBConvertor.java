package com.whosbean.qpush.protobuf.convertor;

import com.whosbean.qpush.core.MessageUtils;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.protobuf.PBAPNSBody;
import com.whosbean.qpush.protobuf.PBAPNSMessage;
import com.whosbean.qpush.protobuf.PBAPNSUserInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Created by Yaming on 2014/10/27.
 */
public class PBConvertor {

    public static PBAPNSMessage fromBean(Payload payload){

        PBAPNSMessage.Builder builder = PBAPNSMessage.newBuilder();

        PBAPNSBody.Builder body = PBAPNSBody.newBuilder();
        body.setAlert(payload.getTitle());
        body.setSound(payload.getSound());
        body.setBadge(payload.getBadge());
        builder.setAps(body);

        if (StringUtils.isBlank(payload.getExtras())) {
            Map<String, String> extra = MessageUtils.asT(Map.class, payload.getExtras());
            for(String key : extra.keySet()){
                String value = extra.get(key);

                PBAPNSUserInfo.Builder ui = PBAPNSUserInfo.newBuilder();
                ui.setKey(key);
                ui.setValue(value);

                builder.addUserInfo(ui);
            }
        }

        return builder.build();
    }


}

package com.whosbean.qpush;

import com.google.common.collect.Lists;
import com.whosbean.qpush.client.AppPayload;
import com.whosbean.qpush.client.QPushClient;

import java.io.IOException;
import java.util.HashMap;

/**
 * Hello world!
 *
 */
public class ClientApp
{

    public static void main( String[] args )
    {
        int i = 0;
        while (i < 10 * 1000){
            send(i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }

        try {
            Thread.sleep(3600 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void send(int seq) {
        AppPayload payload = new AppPayload();
        payload.appkey = "app01";
        payload.title = "this is title " + seq;
        payload.badge = 10;
        payload.broadcast = true;
        payload.sound = "default";
        payload.clients = Lists.newArrayList();
        payload.ext = new HashMap<String, String>();
        payload.ext.put("a", "1");
        payload.ext.put("b", "2");

        try {
            System.out.println("send message. ");
            QPushClient.send(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

QPush
=====

a push server for mobile apps.
it is based on Netty. 

QPush has two major parts. one is *Gateway* which is for maintain connection between mobile apps and Netty. the other is
*Publisher* which is receiving incoming message or payload from app server.

QPush uses *MySQL* as storage. and also uses redis to pipeline the messages out to *Gateway*

[A Simple diagram for the systems!](https://raw.githubusercontent.com/yamingd/QPush/master/Overall.png)

Payload follows Apple's APNS message structure. as following
```
{"aps":{
    "alert": "your message title is here",
    "badge": total number of remind,
    "sound": "the voice file you want to play while receiving message"
    },
"userInfo":{
    "key1": "value1",
    "key2": "value2"
  }
}
```
and at the mean time. Apple's APNS message is limited to *256 bytes*. you should keep this in your heart.
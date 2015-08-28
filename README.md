QPush
=====

a push server for mobile apps.
it is based on Netty. 

QPush has four major parts. 
* first of all, it is **Gateway** which is the most important part, and for maintaining connections between mobile apps and servers. 
* the second part is the **Publisher**, which is receiving incoming messages or payloads from app server.
* the third is **Pipe**, it flows the message from **Publisher** to **Gateway** and to the mobile phone at the end.
* the last one is **Client**, it connects to **Publisher** and keept this connection. **Your App Server** can use **Client** to send message 
to **Publisher** directly, by providing some simple configuration such as the **Publisher** ip address. 
the overall as following( from qpush_client.properties)
```
host=127.0.0.1
port=8082
thread_pool=100
```


QPush uses **MySQL** as storage. and also uses **Redis** to pipeline the messages out to **Gateway**


![A Simple diagram of QPush](https://raw.githubusercontent.com/yamingd/QPush/master/Overall-02.png)


Payload follows Apple's APNS message structure. as following
```
{
"aps":
{
    "alert": "your message title is here",
    "badge": total number of remind,
    "sound": "the voice file you want to play while receiving message"
 },
"userInfo":
{
    "key1": "value1",
    "key2": "value2"
  }
}
```
*userInfo* part is optional.
and at the mean time. Apple's APNS message is limited to **256 bytes**. you should keep that in mind.

### 使用
https://github.com/yamingd/QPush/wiki

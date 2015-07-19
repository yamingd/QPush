#!/bin/sh

cd /home/jetty/source

target="QPush"

if [ ! -d "$target" ]; then
  git clone https://github.com/yamingd/QPush.git
  cd "$target"
else
  cd "$target"
  git pull origin master
fi

# clean first
mvn clean
# compile source
mvn compile
# package
mvn install

\cp -rf /home/jetty/source/QPush/Gateway/target/qpush-gateway-1.0-SNAPSHOT-gateway.zip /home/jetty/qpush/gateway
cd /home/jetty/qpush/gateway
rm -fr lib
rm -fr classes
unzip -o qpush-gateway-1.0-SNAPSHOT-gateway.zip
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/gateway.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/

echo $(date) > /home/jetty/qpush/gateway/deploy.status

rm qpush-gateway-1.0-SNAPSHOT-gateway.zip

\cp -rf /home/jetty/source/QPush/Publisher/target/qpush-publisher-1.0-SNAPSHOT-publisher.zip /home/jetty/qpush/publisher
cd /home/jetty/qpush/publisher
rm -fr lib
rm -fr classes
unzip -o qpush-publisher-1.0-SNAPSHOT-publisher.zip
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/publisher.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/

rm qpush-publisher-1.0-SNAPSHOT-publisher.zip

echo $(date) > /home/jetty/qpush/publisher/deploy.status

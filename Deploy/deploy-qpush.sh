#!/bin/sh

cd /home/jetty/source

target="QPush"
VERSION="1.3"

if [ ! -d "$target" ]; then
  git clone https://github.com/yamingd/QPush.git
  cd "$target"
else
  cd "$target"
  git pull origin master
fi

gradle clean
gradle build
gradle install
gradle createJobJar

\cp -rf /home/jetty/source/QPush/Gateway/build/Gateway-$VERSION-all.jar /home/jetty/qpush/gateway
cd /home/jetty/qpush/gateway
rm -fr lib
rm -fr classes
unzip -o Gateway-$VERSION-all.jar
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/gateway.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/
rm Gateway-$VERSION-all.jar

\cp -rf /home/jetty/source/QPush/Publisher/build/Publisher-$VERSION-all.jar /home/jetty/qpush/publisher
cd /home/jetty/qpush/publisher
rm -fr lib
rm -fr classes
unzip -o Publisher-$VERSION-all.jar
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/publisher.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/

rm Publisher-$VERSION-all.jar

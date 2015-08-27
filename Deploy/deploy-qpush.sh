#!/bin/sh

cd /home/jetty/source

target="QPush"
VERSION="1.1"
SOURCE_FOLDER="/home/jetty/source/QPush"
RUN_FOLDER="/home/jetty/qpush"


if [ ! -d "$target" ]; then
  git clone https://github.com/yamingd/QPush.git
  cd "$target"
else
  cd "$target"
  git pull origin master
fi

# clean first
gradle clean
# compile source
gradle build
# install to .m2
gradle install
# package
gradle createJobJar


\cp -rf $SOURCE_FOLDER/Gateway/build/qpush-gateway-$VERSION-all.jar $RUN_FOLDER/gateway
cd $RUN_FOLDER/gateway
rm -fr lib
rm -fr classes
unzip -o qpush-gateway-$VERSION-all.jar
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/gateway.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/

echo $(date) > $RUN_FOLDER/gateway/deploy.status

rm qpush-gateway-$VERSION-all.jar

\cp -rf $SOURCE_FOLDER/Publisher/build/qpush-publisher-$VERSION-all.jar $RUN_FOLDER/publisher
cd $RUN_FOLDER/publisher
rm -fr lib
rm -fr classes
unzip -o qpush-publisher-$VERSION-all.jar
\cp -rf ../conf/jdbc.properties   ../conf/jedis.properties ../conf/logback.xml ../conf/spring-redis.xml ../conf/spring-db.xml ../conf/publisher.properties classes

\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/libwrapper.so lib/
\cp /home/jetty/wrapper-linux-x86-64-3.5.26/lib/wrapper.jar lib/

rm qpush-publisher-$VERSION-all.jar

echo $(date) > $RUN_FOLDER/publisher/deploy.status

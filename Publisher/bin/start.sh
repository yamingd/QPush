#!/bin/bash

#-------------------------------------------------------------------
#    Mb Bootstrap Script
#-------------------------------------------------------------------

# find Mb home.
CURR_DIR=`pwd`
RESV_HOME=`pwd`

echo $CURR_DIR
cd $CURR_DIR

if [ -z "$RESV_HOME" ] ; then
    echo
    echo Must set RESV_HOME
    echo
    exit 1
fi


for i in $RESV_HOME/lib/*.jar; do
    CLASSPATH=$i:$CLASSPATH;
done

CLASSPATH=$RESV_HOME/classes:$CLASSPATH

DEBUG_INFO=" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n"
DEBUG=""

MAIN_CLASS="com.argo.qpush.publisher.ServerMain";
DEFAULT_OPTS="-server -Xms8G -Xmx8G -Xmn1G -XX:PermSize=50M -XX:MaxPermSize=50M -Xss256k -Dio.netty.leakDetectionLevel=advanced" ;


DEFAULT_OPTS="$DEFAULT_OPTS -XX:+DisableExplicitGC -XX:SurvivorRatio=1 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled -XX:LargePageSizeInBytes=128M -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+PrintClassHistogram -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -Xloggc:gc.log -Dfile.encoding=UTF-8"
DEFAULT_OPTS="$DEFAULT_OPTS -DMB.home=\"$RESV_HOME\""

echo java $DEBUG $DEFAULT_OPTS  -classpath $CLASSPATH  $MAIN_CLASS
java $DEBUG $DEFAULT_OPTS  -classpath $CLASSPATH  $MAIN_CLASS &
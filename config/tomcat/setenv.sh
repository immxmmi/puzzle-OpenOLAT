#!/bin/bash

export CATALINA_BASE=/usr/local
export CATALINA_HOME=$CATALINA_BASE/tomcat
export JRE_HOME=/opt/java/openjdk

export JAVA_XMS="-Xms1024m"
export JAVA_XMX="-Xmx1024m"
export JAVA_META="-XX:MaxMetaspaceSize=512m"
export JAVA_TIMEZONE="-Duser.timezone=Europe/Zurich"
export JAVA_PROFILE="-Dspring.profiles.active=myprofile"
export JAVA_HEADLESS="-Djava.awt.headless=true"
export JAVA_IPV4="-Djava.net.preferIPv4Stack=true"
export JAVA_USER="-Duser.name=openolat"
export JAVA_OOM="-XX:+HeapDumpOnOutOfMemoryError"
export JAVA_DUMP="-XX:HeapDumpPath=."

export CATALINA_OPTS=" \
$JAVA_XMS $JAVA_XMX $JAVA_META $JAVA_USER $JAVA_TIMEZONE \
$JAVA_PROFILE $JAVA_HEADLESS $JAVA_IPV4 $JAVA_OOM $JAVA_DUMP
"
#!/bin/bash

export CATALINA_HOME=/usr/local/tomcat
export CATALINA_BASE=/usr/local/tomcat
export JRE_HOME=/opt/java/openjdk
export CATALINA_PID=/usr/local/tomcat/run/openolat.pid
export CATALINA_TMPDIR=/tmp/openolat

mkdir -p "$CATALINA_TMPDIR"

export CATALINA_OPTS=" \
  -Xmx1024m -Xms512m -XX:MaxMetaspaceSize=512m \
  -Duser.name=openolat \
  -Duser.timezone=Europe/Zurich \
  -Dspring.profiles.active=myprofile \
  -Djava.awt.headless=true \
  -Djava.net.preferIPv4Stack=true \
  -Djava.security.egd=file:/dev/urandom \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=. \
"
#!/bin/bash
set -e

export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-5432}
export DB_USERNAME=${DB_USERNAME:-openolat}
export DB_PASSWORD=${DB_PASSWORD:-openolat}
export DB_NAME=${DB_NAME:-openolat}

envsubst < /usr/local/tomcat/conf/Catalina/localhost/ROOT.template.xml > /usr/local/tomcat/conf/Catalina/localhost/ROOT.xml

exec $CATALINA_HOME/bin/catalina.sh run
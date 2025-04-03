#!/bin/bash
set -e

export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-5432}
export DB_USERNAME=${DB_USERNAME:-openolat}
export DB_PASSWORD=${DB_PASSWORD:-openolat}
export DB_NAME=${DB_NAME:-openolat}

: "${DB_HOST:?Missing DB_HOST}"
: "${DB_USERNAME:?Missing DB_USERNAME}"
: "${DB_PASSWORD:?Missing DB_PASSWORD}"
: "${DB_NAME:?Missing DB_NAME}"

if [ ! -f $CATALINA_HOME/conf/Catalina/localhost/ROOT.template.xml ]; then
  echo "ROOT.template.xml not found!"
  exit 1
fi

echo "Generating ROOT.xml from template..."
envsubst < $CATALINA_HOME/conf/Catalina/localhost/ROOT.template.xml > $CATALINA_HOME/conf/Catalina/localhost/ROOT.xml
echo "Generated ROOT.xml:"
cat $CATALINA_HOME/conf/Catalina/localhost/ROOT.xml

echo "Starting Tomcat..."
echo "Calling catalina.sh directly..."
$CATALINA_HOME/bin/catalina.sh run
exit $?
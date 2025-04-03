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

echo "Starting Tomcat..."
echo "Calling catalina.sh directly..."
exec $CATALINA_HOME/bin/catalina.sh run
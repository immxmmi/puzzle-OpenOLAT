#!/bin/bash
# Description: Extracts OpenOlat version from olat.properties

set -e

PROPERTIES_FILE="src/main/resources/serviceconfig/olat.properties"

if [[ ! -f "$PROPERTIES_FILE" ]]; then
  echo "❌ Properties file not found: $PROPERTIES_FILE"
  exit 1
fi

VERSION=$(grep '^build.version' "$PROPERTIES_FILE" | cut -d'=' -f2 | tr -d '[:space:]')
echo "$VERSION"
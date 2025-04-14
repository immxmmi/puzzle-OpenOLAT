#!/bin/bash
# Description: Check if a Docker registry is reachable

HOST=$1
PORT=$2

if [ -z "$HOST" ]; then
  echo "❌ Registry host is required."
  exit 1
fi

if [ -n "$PORT" ]; then
  ADDRESS="$HOST:$PORT"
else
  ADDRESS="$HOST"
fi

echo "🔍 Checking registry availability: $ADDRESS"

if nc -z -w 5 "$HOST" "$PORT"; then
  echo "✅ Registry $ADDRESS is reachable."
  exit 0
else
  echo "❌ Registry $ADDRESS is not reachable."
  exit 1
fi
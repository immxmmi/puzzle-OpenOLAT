#!/bin/bash
# Description: Builds a Docker image with provided Dockerfile and tag

set -e

DOCKERFILE="$1"
IMAGE_NAME="$2"
TAG="$3"

if [[ -z "$DOCKERFILE" || -z "$IMAGE_NAME" || -z "$TAG" ]]; then
  echo "❌ Missing arguments"
  echo "Usage: $0 <dockerfile> <image-name> <tag>"
  exit 1
fi

docker build -f "$DOCKERFILE" -t "${IMAGE_NAME}:${TAG}" .
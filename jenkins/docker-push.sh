#!/bin/bash
# Description: Tags and pushes Docker image to a Registry

set -e

IMAGE_NAME="$1"
VERSION_TAG="$2"
BRANCH_NAME="$3"
REGISTRY="$4"
USER="$5"
PASS="$6"

if [[ -z "$IMAGE_NAME" || -z "$VERSION_TAG" || -z "$BRANCH_NAME" || -z "$REGISTRY" || -z "$USER" || -z "$PASS" ]]; then
  echo "❌ Missing required arguments"
  echo "Usage: $0 <image> <version> <branch> <registry> <user> <pass>"
  exit 1
fi

echo "$PASS" | docker login "$REGISTRY" -u "$USER" --password-stdin

docker tag "${IMAGE_NAME}:${VERSION_TAG}" "${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
docker tag "${IMAGE_NAME}:${VERSION_TAG}" "${REGISTRY}/${IMAGE_NAME}:${BRANCH_NAME}-latest"

docker push "${REGISTRY}/${IMAGE_NAME}:${VERSION_TAG}"
docker push "${REGISTRY}/${IMAGE_NAME}:${BRANCH_NAME}-latest"
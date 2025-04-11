#!/bin/bash

# Description: Installs Helm, lints chart, updates dependencies, packages and pushes to OCI Registry

set -e

CHART_DIR="$1"
OCI_URL="$2"

if [[ -z "$CHART_DIR" || -z "$OCI_URL" ]]; then
  echo "❌ Missing arguments"
  echo "Usage: $0 <chart-directory> <oci-url>"
  exit 1
fi

# Install Helm (idempotent)
curl -s https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

helm lint "$CHART_DIR"
helm dependency update "$CHART_DIR"
helm package "$CHART_DIR"
mv ./*.tgz chart.tgz

helm push chart.tgz "$OCI_URL" #--insecure-skip-tls-verify
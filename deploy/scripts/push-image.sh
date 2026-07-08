#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-latest}"

echo "Pushing image: fujimiyashion/shining-english-api:${TAG}..."
docker push fujimiyashion/shining-english-api:"${TAG}"

echo "Done."

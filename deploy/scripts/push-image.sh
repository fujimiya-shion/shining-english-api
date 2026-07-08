#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-$(git describe --always --tags)}"

echo "Pushing image: fujimiyashion/shining-english-api:${TAG}..."
docker push fujimiyashion/shining-english-api:"${TAG}"

echo "Pushing image: fujimiyashion/shining-english-api:latest..."
docker tag fujimiyashion/shining-english-api:"${TAG}" fujimiyashion/shining-english-api:latest
docker push fujimiyashion/shining-english-api:latest

echo "Done."

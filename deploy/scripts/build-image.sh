#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-$(git describe --always --tags)}"

echo "Building JAR..."
./gradlew bootJar --no-daemon

echo "Building Docker image: fujimiyashion/shining-english-api:${TAG}..."
docker build -t fujimiyashion/shining-english-api:"${TAG}" -f docker/prod/Dockerfile .

echo "Done. Run push-image.sh ${TAG} to push."

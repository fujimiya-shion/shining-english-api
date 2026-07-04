#!/usr/bin/env bash
set -uo pipefail

cd /workspace

compile_sources() {
  ./gradlew classes processResources --no-daemon
}

until compile_sources; do
  echo "Initial compile failed. Waiting for source changes before retrying..."
  inotifywait \
    --recursive \
    --event close_write,create,delete,move \
    src/main/java \
    src/main/resources
done

./gradlew bootRun --no-daemon &
APP_PID=$!

shutdown() {
  kill "$APP_PID" 2>/dev/null || true
}

trap shutdown EXIT INT TERM

while true; do
  inotifywait \
    --recursive \
    --event close_write,create,delete,move \
    src/main/java \
    src/main/resources

  if ! compile_sources; then
    echo "Compile failed. Keeping current app instance running and waiting for the next change..."
  fi
done

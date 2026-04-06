#!/bin/bash
set -e

source /etc/app/config.env

echo "Waiting for application to start..."

MAX_RETRIES=15
RETRY_INTERVAL=10

HEALTH_OK=false

for i in $(seq 1 $MAX_RETRIES); do
  if curl -sf --max-time 10 "http://localhost:$CONTAINER_PORT/actuator/health" > /dev/null; then
    echo "Health check passed."
    HEALTH_OK=true
    break
  fi
  echo "Health check attempt $i/$MAX_RETRIES failed. Retrying in ${RETRY_INTERVAL}s..."
  sleep $RETRY_INTERVAL
done

if [ "$HEALTH_OK" = "true" ]; then
  exit 0
else
  echo "Health check failed after $MAX_RETRIES attempts."
  exit 1
fi
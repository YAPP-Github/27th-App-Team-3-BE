#!/bin/bash
set -e

source /etc/app/config.env

echo "Waiting for application to start..."
sleep 30

MAX_RETRIES=10
RETRY_INTERVAL=10

for i in $(seq 1 $MAX_RETRIES); do
  if curl -sf --max-time 10 "http://localhost:$CONTAINER_PORT/actuator/health" > /dev/null; then
    echo "Health check passed."
    exit 0
  fi
  echo "Health check attempt $i/$MAX_RETRIES failed. Retrying in ${RETRY_INTERVAL}s..."
  sleep $RETRY_INTERVAL
done

echo "Health check failed after $MAX_RETRIES attempts."
exit 1
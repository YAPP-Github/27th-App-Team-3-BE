#!/bin/bash
set -e

source /etc/app/config.env

echo "Stopping container: $CONTAINER_NAME"
docker stop -t 20 "$CONTAINER_NAME" || true
docker rm "$CONTAINER_NAME" || true
echo "Container stopped."
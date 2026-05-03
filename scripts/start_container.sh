#!/bin/bash
set -e

source /etc/app/config.env

echo "Logging in to ECR..."
aws ecr get-login-password --region "$AWS_REGION" | \
  docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "Fetching credentials from Secrets Manager..."
SECRET=$(aws secretsmanager get-secret-value \
  --secret-id "$SECRET_NAME" \
  --region "$AWS_REGION" \
  --query SecretString \
  --output text)

DB_USERNAME=$(echo "$SECRET" | jq -r '.username')
DB_PASSWORD=$(echo "$SECRET" | jq -r '.password')

echo "Pulling latest image..."
docker pull "$ECR_REGISTRY/$ECR_REPOSITORY:latest"

echo "Starting container..."
docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --memory="1400m" \
  --memory-swap="1400m" \
  -p "$CONTAINER_PORT:$CONTAINER_PORT" \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  -e DB_HOST="$DB_HOST" \
  -e DB_PORT="$DB_PORT" \
  -e DB_NAME="$DB_NAME" \
  -e DB_USERNAME="$DB_USERNAME" \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -e REDIS_HOST="$REDIS_HOST" \
  -e REDIS_PORT="$REDIS_PORT" \
  "$ECR_REGISTRY/$ECR_REPOSITORY:latest"

echo "Container started successfully."

#!/bin/bash
awslocal sqs create-queue \
  --queue-name twix-notification-local \
  --region ap-northeast-2
echo "SQS queue created: twix-notification-local"
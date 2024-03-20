#!/usr/bin/env bash

set -x

export APP_NAME=cf-butler

if cf app $APP_NAME --guid; then
  cf stop $APP_NAME
  cf unbind-service $APP_NAME $APP_NAME-secrets
  cf delete-service $APP_NAME-secrets -f
  cf unbind-service $APP_NAME $APP_NAME-backend
  cf delete-service $APP_NAME-backend -f
  cf delete $APP_NAME -r -f
else
  echo "$APP_NAME does not exist"
fi

#!/usr/bin/env bash

set -e

export APP_NAME=cf-butler



case "$1" in

  --with-credhub | -c)
  cf push --no-start
  cf create-service credhub default $APP_NAME-secrets -c config/secrets.json
  while [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; do
    echo "$APP_NAME-secrets is not ready yet..."
    sleep 5
  done
  cf bind-service $APP_NAME $APP_NAME-secrets
  cf start $APP_NAME
  ;;

  _ | *)
  cf push --no-start
  cf create-user-provided-service $APP_NAME-secrets -p config/secrets.json
  while [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; do
    echo "$APP_NAME-secrets is not ready yet..."
    sleep 5
  done
  cf bind-service $APP_NAME $APP_NAME-secrets
  cf start $APP_NAME
  ;;

esac

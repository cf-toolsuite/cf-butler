#!/usr/bin/env bash

set -e

export APP_NAME=cf-butler

if [ -z "$1" ] && [ -z "$2" ]; then
  echo "Usage: deploy.alt.sh {credential_store_provider_option} {path_to_secrets_file} {additional-cf-push-options}"
  exit 1
fi



case "$1" in

  --with-credhub | -c)
  cf push --no-start "$3"
  if ! cf service $APP_NAME-secrets > /dev/null; then
    cf create-service credhub default $APP_NAME-secrets -c "$2"
    for (( i = 0; i < 90; i++ )); do
      if [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; then
        echo "$APP_NAME-secrets is not ready yet..."
        sleep 10
      else
        break
      fi
    done
  fi
  cf bind-service $APP_NAME $APP_NAME-secrets
  cf start $APP_NAME
  ;;

  _ | *)
  cf push --no-start "$3"
  if ! cf service $APP_NAME-secrets > /dev/null; then
    cf create-user-provided-service $APP_NAME-secrets -p "$2"
    for (( i = 0; i < 90; i++ )); do
      if [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; then
        echo "$APP_NAME-secrets is not ready yet..."
        sleep 10
      else
        break
      fi
    done
  fi
  cf bind-service $APP_NAME $APP_NAME-secrets
  cf start $APP_NAME
  ;;

esac

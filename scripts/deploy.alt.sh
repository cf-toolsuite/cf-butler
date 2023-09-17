#!/usr/bin/env bash

set -e

export APP_NAME=cf-butler

if [ -z "$1" ] && [ -z "$2" ]; then
	echo "Usage: deploy.alt.sh {credential_store_provider_option} {path_to_secrets_file}"
	exit 1
fi



case "$1" in

	--with-credhub | -c)
	cf push --no-start
	cf create-service credhub default $APP_NAME-secrets -c "$2"
	while [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; do
      echo "$APP_NAME-secrets is not ready yet..."
	  sleep 5s
    done
	cf bind-service $APP_NAME $APP_NAME-secrets
	cf start $APP_NAME
	;;

	_ | *)
	cf push --no-start
	cf create-user-provided-service $APP_NAME-secrets -p "$2"
	while [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; do
      echo "$APP_NAME-secrets is not ready yet..."
	  sleep 5s
    done
	cf bind-service $APP_NAME $APP_NAME-secrets
	cf start $APP_NAME
	;;

esac

#!/usr/bin/env bash

set -e

export APP_NAME=cf-butler

if [ -z "$1" ] && [ -z "$2" ]; then
	echo "Usage: deploy.k8s.sh {credential_store_provider_option} {path_to_secrets_file}"
	exit 1
fi

# Cloud Native Buildpacks do not like the Gradle Git plugin... so remove it
mv build.gradle build.default.gradle
cp build.cnb.gradle build.gradle

case "$1" in

	--with-credhub | -c)
	cf push -f manifest.cf4k8s.yml --no-start
	cf create-service credhub default $APP_NAME-secrets -c "$2"
	cf bind-service $APP_NAME $APP_NAME-secrets
	cf start $APP_NAME
	;;

	_ | *)
	cf push -f manifest.cf4k8s.yml --no-start
	cf create-user-provided-service $APP_NAME-secrets -p "$2"
	cf bind-service $APP_NAME $APP_NAME-secrets
	cf start $APP_NAME
	;;

esac

# Restore build
rm build.gradle
mv build.default.gradle build.gradle
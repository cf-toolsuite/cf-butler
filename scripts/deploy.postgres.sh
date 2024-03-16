#!/usr/bin/env bash

# Script assumes VMware Postgres for VMware Tanzu Application Service (https://docs.vmware.com/en/VMware-Postgres-for-VMware-Tanzu-Application-Service/1.1/postgres/index.html) is available as service in cf marketplace
# Feel free to swap out the service for other PostgreSQL providers, like:
#   * Meta Azure Service Broker - https://github.com/Azure/meta-azure-service-broker/blob/master/docs/azure-postgresql-db.md
#   * AWS Service Broker - http://docs.pivotal.io/aws-services/creating.html#rds

set -e

export APP_NAME=cf-butler


cf push --no-start
cf create-user-provided-service $APP_NAME-secrets -p config/secrets.json
cf create-service postgres on-demand-postgres-db $APP_NAME-backend
while [[ $(cf service $APP_NAME-secrets) != *"succeeded"* ]]; do
    echo "$APP_NAME-secrets is not ready yet..."
    sleep 5s
done
cf bind-service $APP_NAME $APP_NAME-secrets
while [[ $(cf service $APP_NAME-backend) != *"succeeded"* ]]; do
    echo "$APP_NAME-backend is not ready yet..."
    sleep 5s
done
cf bind-service $APP_NAME $APP_NAME-backend
cf start $APP_NAME

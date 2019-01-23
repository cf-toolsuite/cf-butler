#!/usr/bin/env bash

## Create environment on PCF One consisting of one org and 2 spaces
## Deploys a collection of apps and services then deploys cf-butler
## Meant to work within 10G quota restriction

set -e

# Change me
export ORGS=( pivot-cphillipson )
export TODAY=2019-01-22-74FEC3B0-8C74-41F3-91EE-1B137CE91BBB
export DELAY=600

cd ../../../
mkdir -p cf-butler-demo-$TODAY
cd cf-butler-demo-$TODAY

export SPACES=( dev test )
export REPOS1=( devops-workshop )
export REPOS2=( track-shipments )
export REPOS3=( reactive-jdbc-demo )
export REPOS4=( spring-boot-gzip-compression-example )


## Deployment Phase

for o in "${ORGS[@]}"
do
   #cf create-org $o

   for s in "${SPACES[@]}"
   do
      #cf create-space $s
      cf target -o $o -s $s

      ## Create app not bound to any services, leave it running
      for r in "${REPOS2[@]}"
      do
         cd $r
         cf push $r-$s -b java_buildpack_offline -s cflinuxfs3 -m 1G -i 1
         cd ..
      done

      ## Create app not bound to any services, then stop it
      for x in "${REPOS4[@]}"
      do
         cd $x
         cf push $x-$s -b java_buildpack_offline -s cflinuxfs3 -m 1G -i 1
         cf stop $x-$s
         cd ..
      done

      ## Create apps and bind to services, then stop apps
      for q in "${REPOS3[@]}"
      do
         cd $q
         cf push $q-$s -b java_buildpack_offline -p build/libs/reactive-jdbc-demo-0.0.1-SNAPSHOT.jar -s cflinuxfs3 -m 1G -i 1 --no-start
         cf create-user-provided-service postgres-$s-secrets -p ../../cf-butler/tests/demo-script/postgres.json
         cf bind-service $q-$s postgres-$s-secrets
         cf start $q-$s
         cf stop $q-$s
         cd ..
      done

      ## Create app bound to service, leave it running
      for p in "${REPOS1[@]}"
      do
         cd $p/labs/solutions/03/cloud-native-spring
         cf push $p-$s -b java_buildpack_offline -s cflinuxfs3 -m 1G -i 1 --no-start
         cf create-service p.mysql db-small mysql-$s
         sleep $DELAY
         cf bind-service $p-$s mysql-$s
         cf start cloud-native-spring
         cd ../../../../..
      done

      ## Create service orphans 
      cf create-service p.redis cache-small redis-$s
      sleep $DELAY
      # cf create-service p-service-registry standard service-registry-s$
      # sleep $DELAY

   done
done


## Deploy cf-butler
cf target -o pivot-cphillipson -s system
cd cf-butler
cf push --no-start
cf create-service credhub default cf-butler-secrets -c config/secrets.pcfone.json
cf bind-service cf-butler cf-butler-secrets
cf start cf-butler
cd ..


## Deploy cf-app-inventory-report
# cd cf-app-inventory-report
# cf push --no-start
# cf create-service credhub default cf-app-inventory-report-secrets -c config/secrets.pcfone.json
# cf bind-service cf-app-inventory-report cf-app-inventory-report-secrets
# cf start cf-app-inventory-report
# cd ..


## Deploy cf-app-inventory-report
# cd cf-service-inventory-report
# cf push --no-start
# cf create-service credhub default cf-service-inventory-report-secrets -c config/secrets.pcfone.json
# cf bind-service cf-service-inventory-report cf-service-inventory-report-secrets
# cf start cf-service-inventory-report
# cd ..

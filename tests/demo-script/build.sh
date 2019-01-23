#!/usr/bin/env bash

## Clones and builds projects for demo purposes

## Pre-requisites
## - Git 2.20.1
## - Open JDK 11
## - Gradle 5.1.1

set -e

# Change me
export TODAY=`date +%Y-%m-%d`

cd ../../../
mkdir -p cf-butler-demo-$TODAY
cd cf-butler-demo-$TODAY

export REPOS1=( devops-workshop )
export REPOS2=( track-shipments )
export REPOS3=( jdbc-demo reactive-jdbc-demo )
export REPOS4=( spring-boot-gzip-compression-example )


## Build Phase

## Create app not bound to any services, leave it running
for r in "${REPOS2[@]}"
do
   git clone https://github.com/pacphi/$r
   cd $r
   gradle clean build
   cd ..
done

## Create app not bound to any services, then stop it
for x in "${REPOS4[@]}"
do
   git clone https://github.com/pacphi/$x
   cd $x
   gradle clean build
   cd ..
done

## Create apps and bind to services, then stop apps
for q in "${REPOS3[@]}"
do
   git clone https://github.com/pacphi/$q
   cd $q
   gradle clean build
   cd ..
done

## Create app bound to service, leave it running
for p in "${REPOS1[@]}"
do
   git clone https://github.com/Pivotal-Field-Engineering/$p
   cd $p/labs/solutions/03/cloud-native-spring
   gradle clean build
   cd ../../../../..
done


## cf-butler
git clone https://github.com/pacphi/cf-butler
cd cf-butler
gradle clean build
cd ..


## cf-app-inventory-report
git clone https://github.com/pacphi/cf-app-inventory-report
cd cf-app-inventory-report
gradle clean build
cd ..


## cf-app-inventory-report
git clone https://github.com/pacphi/cf-service-inventory-report
cd cf-service-inventory-report
gradle clean build
cd ..

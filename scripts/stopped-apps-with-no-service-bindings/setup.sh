#!/usr/bin/env bash

set -e

export ORG=lighthouse
export SPACE=qa
export TODAY=`date +%Y-%m-%d`

mkdir -p tmp/$TODAY
cd tmp/$TODAY

repos=( spring-boot-gzip-compression-example track-shipments)

cf target -o $ORG -s $SPACE

for i in "${repos[@]}"
do
   git clone https://github.com/pacphi/$i
   cd $i
   gradle clean build
   cf push $i -b java_buildpack_offline -s cflinuxfs3 -m 1G --random-route
   cd ..
done

cf stop spring-boot-gzip-compression-example

# We expect to see that after executing application-policy that stopped app is removed


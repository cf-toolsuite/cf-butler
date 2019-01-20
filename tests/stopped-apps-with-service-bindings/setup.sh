#!/usr/bin/env bash

set -e

export ORG=lighthouse
export SPACE=qa
export TODAY=`date +%Y-%m-%d`-$(uuidgen)

mkdir -p tmp/$TODAY
cd tmp/$TODAY

cf target -o $ORG -s $SPACE

git clone https://github.com/tezizzm/SmbDemoCore
cd SmbDemoCore
cd src/LighthouseWebCore
cp ../../../../../smb-base-config.json .
cp ../../../../../smb-binding-config.json .
dotnet publish -o ./publish
cf create-service smbvolume Existing winfs-share -c smb-base-config.json
cf push -b dotnet_core_buildpack -p ./publish -s cflinuxfs3 -m 256M --random-route --health-check-type http --no-start
cf bind-service lighthousewebuicore winfs-share -c smb-binding-config.json
cf start lighthousewebuicore
cf stop lighthousewebuicore
cd ..

# We expect to see that after executing application-policy that the stopped app is unbound and removed 
# and service instance is removed too (if policy has delete-services set to true )


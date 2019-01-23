#!/usr/bin/env bash

## Deletes top-level directory hosting all source that was cloned and binaries that were built for demo purposes

set -e

# Change me
export TODAY=`date +%Y-%m-%d`

cd ../../../
rm -Rf cf-butler-demo-$TODAY

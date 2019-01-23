#!/usr/bin/env bash

## Deletes spaces within an existing organization

## Prerequisites
## - authenticated as a user who has role authorizing deletion of spaces within an existing named organization

set -e

export ORG=pivot-cphillipson
export SPACES=(dev test stage)

for s in "${SPACES[@]}"
do
    cf delete-space $s -f
done

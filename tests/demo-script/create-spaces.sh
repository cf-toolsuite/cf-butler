#!/usr/bin/env bash

## Creates spaces within an existing organization

## Prerequisites
## - authenticated as a user who has role authorizing creation of spaces within an existing named organization

set -e

export ORG=pivot-cphillipson
export SPACES=(dev test stage)

for s in "${SPACES[@]}"
do
    cf create-space $s -o $ORG
done

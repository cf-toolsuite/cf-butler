#!/usr/bin/env bash

set -e

export ORG=lighthouse
export SPACE=qa

cf delete-space $SPACE -o $ORG -f

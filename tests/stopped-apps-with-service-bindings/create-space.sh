#!/usr/bin/env bash

set -e

export ORG=lighthouse
export SPACE=qa

cf create-space $SPACE -o $ORG

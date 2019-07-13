#!/usr/bin/env bash

set -e

# **IMPORTANT**
# This script REQUIRES a distribution of Java JDK 8
# in order to complete compiling, packaging and installing the library successfully

mkdir -p tmp
cd tmp
rm -Rf r2dbc-mysql
rm -Rf "${HOME}/.m2"
git clone https://github.com/mirromutth/r2dbc-mysql.git
cd r2dbc-mysql
./mvnw clean install -Dmaven.test.skip=true

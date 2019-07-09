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

# FIXME
# As of 2019-07-08, this fix is in place for reporting ServerVersion so we can connect to p.mysql service instances
cp -f ../../src/third-party/java/io/github/mirromutth/r2dbc/mysql/ServerVersion.java src/main/java/io/github/mirromutth/r2dbc/mysql

./mvnw clean install -Dmaven.test.skip=true

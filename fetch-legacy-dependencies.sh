#!/usr/bin/env bash

# Fetch legacy dependencies script
# Stop-gap measure until Spring Cloud Hoxton and Spring Boot 2.2 go GA

# Useful for those attempting to
#  a) resolve dependencies for the first time after May 8, 2019
#  b) restore dependencies after wiping out or corrupting your Gradle cache

# Fills in the gap for resolving legacy BUILD-SNAPSHOT dependencies that are no longer hosted on https://repo.spring.io
# Until the latest release of Spring Cloud catches up with Spring Boot 2.2, we introduce this "hack" to maintain backward compatibility with Spring Boot 2.1

export TAG=b20190521.1

cd $HOME/Downloads
rm -Rf tmp
mkdir tmp
cd tmp
wget https://github.com/pacphi/cf-butler/releases/download/$TAG/cf-butler-0.1-SNAPSHOT.jar
jar xvf cf-butler-0.1-SNAPSHOT.jar

mkdir -p $HOME/.m2/repository/org/springframework/data/spring-data-r2dbc/1.0.0.BUILD-SNAPSHOT
cp BOOT-INF/lib/spring-data-r2dbc-1.0.0.BUILD-20190508.084637-64.jar $HOME/.m2/repository/org/springframework/data/spring-data-r2dbc/1.0.0.BUILD-SNAPSHOT/spring-data-r2dbc-1.0.0.BUILD-20190508.084637-64.jar

mkdir -p $HOME/.m2/repository/org/springframework/boot/experimental/spring-boot-autoconfigure-r2dbc/0.1.0.BUILD-SNAPSHOT
cp BOOT-INF/lib/spring-boot-autoconfigure-r2dbc-0.1.0.BUILD-SNAPSHOT.jar $HOME/.m2/repository/org/springframework/boot/experimental/spring-boot-autoconfigure-r2dbc/0.1.0.BUILD-SNAPSHOT/spring-boot-autoconfigure-r2dbc-0.1.0.BUILD-20190507.184651-3.jar

mkdir -p $HOME/.m2/repository/org/springframework/boot/experimental/spring-boot-starter-data-r2dbc/0.1.0.BUILD-SNAPSHOT
cp BOOT-INF/lib/spring-boot-starter-data-r2dbc-0.1.0.BUILD-SNAPSHOT.jar $HOME/.m2/repository/org/springframework/boot/experimental/spring-boot-starter-data-r2dbc/0.1.0.BUILD-SNAPSHOT/spring-boot-starter-data-r2dbc-0.1.0.BUILD-20190507.184709-3.jar

cd ..
rm -Rf tmp
cd ..
rm -Rf cf-butler-0.1-SNAPSHOT.jar
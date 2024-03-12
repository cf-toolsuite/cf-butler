# VMware Tanzu Application Service > Butler

## How to Run with Maven

```
./mvnw spring-boot:run -Dspring.profiles.active={target_foundation_profile}
```
where `{target_foundation_profile}` is something like `pcfone`

> You'll need to manually stop to the application with `Ctrl+C`


## How to run w/ Java

```
SPRING_PROFILES_ACTIVE=on-demand,cloud java -jar target/cf-butler-1.0-SNAPSHOT.jar -Djava.security.egd=file:///dev/urandom -Dspring.h2.console.enabled=true -Djava.artifacts.fetch.mode=obtain-jars-from-runtime-metadata -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=1 -XX:+UseStringDeduplication -Xmx3G -XX:MaxDirectMemorySize=1G --spring.config.location=classpath:/application.yml,file:./config/application-dhaka.yml
```

In the example above we have target specific configuration maintained in a `config` folder which would be a sibling of the `target` folder where the executable jar file is expected to be found.  We're setting an environment variable as well as supplying JVM arguments.


## How to Run with Docker

You might choose this option when experimenting with an external database provider image like [postgres](https://github.com/docker-library/postgres/blob/master/15/alpine/Dockerfile) or [mysql](https://github.com/docker-library/mysql/blob/master/8.0/Dockerfile.debian)

Build

```
docker build -t pivotalio/cf-butler:latest .
```

Run

Start database

```
docker run --name butler-mysql -e MYSQL_DATABASE=butler -e MYSQL_ROOT_PASSWORD=p@ssw0rd! -e MYSQL_USER=butler -e MYSQL_PASSWORD=p@ssw0rd -p 3306:3306 -d mysql:8.0.32
```
> MySQL

or

```
docker run --name butler-postgres -e POSTGRES_DB=butler -e POSTGRES_USER=butler -e POSTGRES_PASSWORD=p@ssw0rd -p 5432:5432 -d postgres:15.2
```
> PostgreSQL


Start application

```
docker run -p:8080:8080 -it -e PIVNET_API-TOKEN=xxx -e CF_TOKEN-PROVIDER=sso -e CF_API-HOST=api.run.pcfone.io -e CF_REFRESH-TOKEN=xxx cf-butler:1.0-SNAPSHOT
```
> **Note**: The environment variables declared above represent a minimum required to authorize cf-butler to collect data from [Dhaka](https://login.sys.dhaka.cf-app.com/).  Consult the `*.json` secret file [samples](../samples) for your specific needs to vary behavior and features.

Stop

```
docker ps -a
docker stop {pid}
```
> where `{pid}` is a Docker process id

Cleanup

```
docker rm {pid}
```
> where `{pid}` is the Docker process id

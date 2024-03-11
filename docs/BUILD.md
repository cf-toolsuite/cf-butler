# VMware Tanzu Application Service > Butler

## How to Build

```
./mvnw clean package
```
> Defaults to H2 in-memory backend

### Alternatives

The below represent a collection of Maven profiles available in the Maven POM.

* MySQL (mysql)
  * adds a dependency on [r2dbc-mysql](https://github.com/asyncer-io/r2dbc-mysql)
* Postgres (postgres)
  * adds a dependency on [r2dbc-postrgesql](https://github.com/pgjdbc/r2dbc-postgresql)
* Log4J2 logging (log4j2)
  * swaps out [Logback](http://logback.qos.ch/documentation.html) logging provider for [Log4J2](https://logging.apache.org/log4j/2.x/manual/async.html) and [Disruptor](https://lmax-exchange.github.io/disruptor/user-guide/index.html#_introduction)
* Native image (native)
  * uses [Spring AOT](https://docs.spring.io/spring-native/docs/current/reference/htmlsingle/#spring-aot-maven) to compile a native executable with [GraalVM](https://www.graalvm.org/docs/introduction/)


```
./mvnw clean package -Drdbms=mysql
```
> Work with MySQL backend

```
./mvnw clean package -Drdbms=postgres
```
> Work with Postgres backend

```
./mvnw clean package -Plog4j2
```
> Swap out default "lossy" logging provider


```
# Using Cloud Native Buildpacks image
./mvnw spring-boot:build-image -Pnative

# Using pre-installed Graal CE
./mvnw native:compile -Pnative -DskipTests
```

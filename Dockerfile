FROM azul/zulu-openjdk-alpine:17-latest
LABEL author=cphillipson@pivotal.io
COPY target/cf-butler-1.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
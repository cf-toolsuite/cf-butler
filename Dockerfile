FROM azul/zulu-openjdk-alpine:17.0.9-17.46.19-jre-headless-x86
LABEL author=cphillipson@pivotal.io
COPY target/cf-butler-1.0-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
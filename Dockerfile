FROM openjdk:11.0.4-jre-slim
LABEL author=cphillipson@pivotal.io
COPY build/libs/cf-butler-0.1-SNAPSHOT.jar /app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
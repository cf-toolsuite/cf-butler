# VMware Tanzu Application Service > Butler

## How to check code quality with Sonarqube

Launch an instance of Sonarqube on your workstation with Docker

```
docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 sonarqube
```

Then make sure to add goal and required arguments when building with Maven. For example:

```
mvn sonar:sonar -Dsonar.token=cf-butler -Dsonar.login=admin -Dsonar.password=admin
```

Then visit `http://localhost:9000` in your favorite browser to inspect results of scan.
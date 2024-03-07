# VMware Tanzu Application Service > Butler

[![GA](https://img.shields.io/badge/Release-GA-darkgreen)](https://img.shields.io/badge/Release-GA-darkgreen) ![Github Action CI Workflow Status](https://github.com/cf-toolsuite/cf-butler/actions/workflows/ci.yml/badge.svg) [![Known Vulnerabilities](https://snyk.io/test/github/cf-toolsuite/cf-butler/badge.svg?style=plastic)](https://snyk.io/test/github/cf-toolsuite/cf-butler) [![Release](https://jitpack.io/v/cf-toolsuite/cf-butler.svg)](https://jitpack.io/#cf-toolsuite/cf-butler/master-SNAPSHOT) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


You are a platform operator working for a Fortune 500 enterprise.  You've witnessed first-hand how the product development teams your team supports are super productive; happily invoking `cf push`, `cf create-service` and `cf bind-service` many times per day to deploy applications, create services and bind them to those applications.

This is great, except that over time, on your non-production foundations, as you've browsed organizations and spaces, you have noticed a large number of stopped application instances and orphaned services (i.e., those not bound to any applications).

Reaching out to each development team to tell them to clean-up has become a chore.  Why not implement some automation that allows you a) to obtain snapshot and usage reports and b) define and enforce some house-keeping policies for your non-production foundations where applications and services are perhaps more volatile and c) easily handle multi-organization or system-wide use-cases like application instance scaling or stack changes?

This is where `cf-butler` has your back.

# Table of Contents

  * [What does it do?](#what-does-it-do)
      * [Tell me, don't show me](#tell-me-dont-show-me)
      * [And what about VMware Tanzu Telemetry Collector?](#and-what-about-vmware-tanzu-telemetry-collector)
  * [Prerequisites](#prerequisites)
  * [Tools](#tools)
  * [Clone](#clone)
  * [How to configure](#how-to-configure)
      * [Managing secrets](#managing-secrets)
      * [Minimum required keys](#minimum-required-keys)
        * [Username and password](#username-and-password)
        * [Single-sign on](#single-sign-on)
      * [General configuration notes](#general-configuration-notes)
      * [Using an external database](#using-an-external-database)
      * [Managing policies](#managing-policies)
        * [Hygiene Policies](#hygiene-policies)
        * [Legacy policies](#legacy-policies)
        * [Endpoint policies](#endpoint-policies)
        * [Query policies](#query-policies)
      * [To set the operations schedule](#to-set-the-operations-schedule)
      * [To discriminate user from service accounts](#to-discriminate-user-from-service-accounts)
      * [Filtering organizations](#filtering-organizations)
        * [Blacklist](#blacklist)
        * [Whitelist](#whitelist)
      * [Integration w/ Operations Manager](#integration-w-operations-manager)
  * [How to Build](#how-to-build)
      * [Alternatives](#alternatives)
  * [How to Run with Maven](#how-to-run-with-maven)
  * [How to Run with Docker](#how-to-run-with-docker)
  * [How to check code quality with Sonarqube](#how-to-check-code-quality-with-sonarqube)
  * [How to deploy to VMware Tanzu Application Service](#how-to-deploy-to-vmware-tanzu-application-service)
      * [with Username and password authorization](#with-username-and-password-authorization)
      * [with SSO authorization](#with-sso-authorization)
      * [using scripts](#using-scripts)
  * [Endpoints](#endpoints)
      * [Operations Manager](#operations-manager)
      * [VMware Tanzu Network](#vmware-tanzu-network)
      * [Events](#events)
      * [Metadata](#metadata)
      * [Snapshot](#snapshot)
      * [Accounting](#accounting)
      * [Policies](#policies)
      * [Collection and Execution](#collection-and-execution)
  * [Credits](#credits)

## What does it do?

Please take 5-10 mintues to view this short video demonstration to get a sense of what `cf-butler` can do.

[![Youtube screenshot](cf-butler-demo.jpg)](https://youtu.be/IyLJfC6N60Q)

### Tell me, don't show me

Cf-butler exposes a number of self-service endpoints that perform house-keeping for your foundation.  You define policies and an execution schedule.  E.g., applications and service instances could be removed based on policy crtieria.  Cf-butler also provides detail and summary snapshot reporting on all applications, service instances, user accounts, organizations and spaces.  Lastly, cf-butler [aspires](https://github.com/cf-toolsuite/cf-butler/issues/62) to provide operators insight into the "freshness" of installed tiles, stemcells and buildpacks.

### And what about VMware Tanzu Telemetry Collector?

[VMware Tanzu Telemetry Collector](https://docs.vmware.com/en/Tanzu-Telemetry-for-Ops-Manager/1.2/telemetry-documentation/GUID-index.html) supports collection of configuration data from Operations Manager, certificate data from Credhub, and usage data from VMware Tanzu Application Service.  Customers download and install a [CLI](https://network.pivotal.io/products/pivotal-telemetry-collector/) from VMware Tanzu Network.  Typically, a [Concourse](https://concourse-ci.org) [pipeline](https://docs.pivotal.io/platform-automation/v5.1/inputs-outputs.html#telemetry) is configured to automate collection.  The result of collection is a foundation details [tarball](https://docs.vmware.com/en/Tanzu-Telemetry-for-Ops-Manager/1.2/telemetry-cli-documentation/GUID-data.html#data-collected-2). Customers may opt to transmit this data to VMware Tanzu.

> Note: [Pivotal Telemetry](https://tanzu.vmware.com/legal/telemetry) and [VMware's Customer Experience Improvement Program](https://www.vmware.com/solutions/trustvmware/ceip.html) are opt-in.

Cf-butler is configured and deployed as an application instance. Its capabilities overlap only on usage data collection from VMware Tanzu Application Service.  However, cf-butler performs other useful duties like a) snapshot usage reporting and b) policy registration and execution.


## Prerequisites

Required

* Access to a foundation with VMware Tanzu Application Service 2.11 or better installed
  * and [VMware Tanzu Application Service](https://tanzu.vmware.com/platform/vmware-tanzu-application-service) admin account credentials

Optional

* [VMware Tanzu Network](https://network.pivotal.io) account credentials
* [VMware Tanzu Operations Manager](https://tanzu.vmware.com/platform/pcf-components/pcf-ops-manager) admin account credentials (or client id and client secret)


## Tools

* [git](https://git-scm.com/downloads) 2.40.0 or better
* [JDK](http://openjdk.java.net/install/) 21 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 8.6.1 or better
* [uaac](https://github.com/cloudfoundry/cf-uaac) 4.14.0 or better


## Clone

```
git clone https://github.com/cf-toolsuite/cf-butler.git
```


## How to configure

Make a copy of then edit the contents of the `application.yml` file located in `src/main/resources`.  A best practice is to append a suffix representing the target deployment environment (e.g., `application-pcfone.yml`). You will need to provide administrator credentials to Apps Manager for the foundation if you want the butler to keep your entire foundation tidy.

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and/or [encrypting](https://blog.novatec-gmbh.de/encrypted-properties-spring/) this configuration.

### Managing secrets

Place secrets in `config/secrets.json`, e.g.,

```
{
  "PIVNET_API-TOKEN": "xxxxx"
  "CF_API-HOST": "xxxxx",
  "CF_USERNAME": "xxxxx",
  "CF_PASSWORD": "xxxxx",
}
```

We'll use this file later as input configuration for the creation of either a [credhub](https://docs.vmware.com/en/CredHub-Service-Broker/services/credhub/GUID-using.html) or [user-provided](https://docs.cloudfoundry.org/devguide/services/user-provided.html#credentials) service instance.

> Replace occurrences of `xxxxx` above with appropriate values

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a VMware Tanzu Application Service API endpoint
* `cf.tokenProvider` - VMware Tanzu Application Service authorization token provider, options are: `userpass` or `sso`
* `pivnet.apiToken` - a VMware Tanzu Network legacy API Token, visit your [profile](https://network.pivotal.io/users/dashboard/edit-profile)

Based on choice of the authorization token provider

#### Username and password

* `cf.username` - a VMware Tanzu Application Service account username (typically an administrator account)
* `cf.password` - a VMware Tanzu Application Service account password

#### Single-sign on

* `cf.refreshToken` - the refresh token to be found within `~/.cf/config.json` after your authenticate

### General configuration notes

If you copied and appended a suffix to the original `application.yml` then you would set `spring.profiles.active` to be that suffix

E.g., if you had a configuration file named `application-pcfone.yml`

```
./mvnw spring-boot:run -Dspring.profiles.active=pcfone
```

> See the [samples](samples) directory for a few examples of configuration options when deploying to a foundation.

For an exhaustive listing of all overridable configuration properties consult [ButlerCfEnvProcessor.java](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/config/ButlerCfEnvProcessor.java).

### Using an external database

By default `cf-butler` employs an in-memory [H2](http://www.h2database.com) instance.

If you wish to configure an external database you must set set `spring.r2dbc.*` properties as described [here](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/r2dbc/R2dbcProperties.html).

Before you `cf push`, stash the credentials for your database in `config/secrets.json` like so

```
{
  "R2DBC_URL": "rdbc:<database-provider>://<server>:<port>/<database-name>",
  "R2DBC_USERNAME": "<username>",
  "R2DBC_PASSWORD": "<password>"
}
```

> Replace place-holders encapsulated in `<>` above with real credentials

Or you may wish to `cf bind-service` to a database service instance. In this case you must abide by a naming convention. The name of your service instance must be `cf-butler-backend`.

[DDL](https://en.wikipedia.org/wiki/Data_definition_language) scripts for each supported database provider are managed underneath [src/main/resources/db](src/main/resources/db). Supported databases are: [h2](src/main/resources/db/h2/schema.ddl), [mysql](src/main/resources/db/mysql/schema.ddl) and [postgresql](src/main/resources/db/postgresql/schema.ddl).

> Review the sample scripts for deploying [postgres](scripts/deploy.postgres.sh) and [mysql](scripts/deploy.mysql.sh).  And consult the corresponding secrets samples for [postgres](samples/secrets.pws.with-postgres.json) and [mysql](samples/secrets.pws.with-mysql.json) when you intend to transact an externally hosted database.

### Managing policies

Creation and deletion of policies are managed via API endpoints by default. When an audit trail is important to you, you may opt to set `cf.policies.git.uri` -- this property specifies the location of the repository that contains policy files in JSON format.

When you do this, you shift the lifecycle management of policies to Git.  You will have to specify additional configuration, like

* `cf.policies.git.commit` the commit id to pull from
  * if this property is missing the latest commit will be used
* `cf.policies.git.filePaths` an array of file paths of policy files

If you want to work with a private repository, then you will have to specify

* `cf.policies.git.username`
* `cf.policies.git.password`

one or both are used to authenticate.  In the case where you may have configured a personal access token, set `cf.policies.git.username` equal to the value of the token.

Policy files must adhere to a naming convention where:

* a filename ending with `-AP.json` encapsulates an individual [ApplicationPolicy](src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java)
* a filename ending with `-SIP.json` encapsulates an individual [ServiceInstancePolicy](src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java)

A sample Github repository exists [here](https://github.com/cf-toolsuite/cf-butler-sample-config).

Have a look at [secrets.pws.json](samples/secrets.pws.json) for an example of how to configure secrets for deployment of `cf-butler` to PAS integrating with the aforementioned sample Github repository.

On startup `cf-butler` will read files from the repo and cache in a database.  Each policy's id will be set to the commit id.

#### Hygiene Policies

Hygiene policies are useful when you want to search for and report on dormant workloads, notifying both the operator and for each workload the author and/or his/her space compadres.  Workloads are running applications and service instances that have not been updated in N or more days from the date/time of the policy execution.

> Note: hygiene policy configuration has a special case where if the `days-since-last-update` property value is set to `-1` then ALL workloads (minus the blacklist) are included in the respective notifications.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-HP.json` encapsulates an individual [HygienePolicy](src/main/java/io/pivotal/cfapp/domain/HygienePolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).


#### Legacy Policies

Legacy policies are useful when you want to search for and report on applications deployed to a legacy stack (e.g., windows2012R2, cflinuxfs2) or service offering (e.g., using a product slug name like p-config-server, p-service-registry, p-mysql), notifying both the operator and for each application the author and/or his/her space compadres.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-LP.json` encapsulates an individual [LegacyPolicy](src/main/java/io/pivotal/cfapp/domain/LegacyPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).


#### Resource Notification Policies

Resource Notification policies are useful when you want to generate a report containing resources of a particular type and send that report to email recipients identified by one or more label values ascribed to the those resources.

For example, you might notify primary and/or secondary owners of a collection of a resources.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-RNP.json` encapsulates an individual [ResourceNotificationPolicy](src/main/java/io/pivotal/cfapp/domain/ResourceNotificationPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).

#### Endpoint Policies

Endpoint policies are useful when you want to exercise any of the available GET endpoints and have the results sent to one or more designated email recipients.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-EP.json` encapsulates an individual [EndpointPolicy](src/main/java/io/pivotal/cfapp/domain/EndpointPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).


#### Query policies

Query policies are useful when you want to step out side the canned snapshot reporting capabilties and leverage the underlying [schema](https://github.com/cf-toolsuite/cf-butler/tree/master/src/main/resources/db) to author one or more of your own queries and have the results delivered as comma-separated value attachments using a defined email notification [template](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/EmailNotificationTemplate.java).

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-QP.json` encapsulates an individual [QueryPolicy](src/main/java/io/pivotal/cfapp/domain/QueryPolicy.java)

If you intend to deploy query policies you must also configure the `notification.engine` property.  You can define it in your

application-{env}.yml

```
notification:
  engine: <engine>
```

or

secrets-{env}.json

```
  "NOTIFICATION_ENGINE": "<engine>"
```

> Replace `<engine>` above with one of either `java-mail`, or `sendgrid`

Furthermore, you will need to define additional properties depending on which engine you chose.  Checkout [application.yml](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/resources/application.yml) to get to know what they are.

E.g, if you intended to use [sendgrid](https://www.sendgrid.com) as your email notification engine then your secrets-{env}.yml might contain

```
  "NOTIFICATION_ENGINE": "sendgrid",
  "SENDGRID_API-KEY": "replace_me"
```

### To set the operations schedule

Update the value of the `cron` properties in `application.yml`.  Consult this [article](https://riptutorial.com/spring/example/21209/cron-expression) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.

> `cron` has two sub-properties: `collection` and `execution`.  Make sure `execution` is scheduled to trigger after `collection`.

### To discriminate user from service accounts

Consult [PasSettings.java](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/config/PasSettings.java#L25) for the default pattern value used to discriminate between user and service accounts.  You may override the default by adding to

* application.yml

  ```
  cf:
    accountRegex: "some other pattern"
  ```

or

* config/secrets.json

  ```
  {
    "CF_ACCOUNT-REGEX": "some other pattern"
  }
  ```

### Filtering organizations

#### Blacklist

Set `cf.organizationBlackList`.  The `system` organization is excluded by default.

Edit `application.yml` and add

```
cf:
  organizationBlackList:
    - system
```

or

Add an entry in your `config/secrets.json` like

```
  "CF_ORGANIZATION-BLACK-LIST": [ "system" ]
```

#### Whitelist

Within each [ApplicationPolicy](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java), [ServiceInstancePolicy](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java), or [HygienePolicy](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/HygienePolicy.java) you may optionally specify a list of organizations that will be whitelisted.  Policy execution will be restricted to just these organizations in the whitelist.

> If the organization whitelist is not specified in a policy then that policy's execution applies to all organizations on the foundation (except for those in the organization blacklist).


### Filtering spaces

> Note: if you activate a space blacklist it will take precedence over an organization blacklist! Only one type of blacklist may be in effect.

#### Blacklist

Set `cf.spaceBlackList`.

Edit `application.yml` and add

```
cf:
  spaceBlackList:
    - "orgA:spaceA"
    - "orgB:spaceB"
```

or

Add an entry in your `config/secrets.json` like

```
  "CF_SPACE-BLACK-LIST": [ "orgA:spaceA", "orgB:spaceB" ]
```

> All spaces you define in the blacklist are excluded from consideration by policies.

### Integration w/ Operations Manager

You must add the following configuration properties to `application-{env}.yml` if you want to enable integration with an Operations Manager instance

* `om.apiHost` - a VMware Tanzu Operations Manager API endpoint
* `om.enabled` - a boolean property that must be set to `true`
* `om.grantType` - [Token](https://docs.cloudfoundry.org/api/uaa/version/75.4.0/index.html#token) grant type

If `om.grantType` is set to `password`

* `om.username` - username for Operations Manager admin account
* `om.password` - password for Operations Manager admin account
* `om.clientId` - must be set to `opsman`
* `om.clientSecret` - must be set to blank

If `om.grantType` is set to `client_credentials`

* `om.username` - must be set to blank
* `om.password` - must be set to blank
* `om.clientId` - the recipient of the token
* `om.clientSecret` - the secret passphrase configured for the OAuth client

> the `{env}` filename suffix above denotes the Spring Profile you would activate for your environment

or

Add entries in your `config/secrets.json` like

```
  "OM_API-HOST": "xxxxxx",
  "OM_ENABLED": true
```

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


## How to Run with Maven

```
./mvnw spring-boot:run -Dspring.profiles.active={target_foundation_profile}
```
where `{target_foundation_profile}` is something like `pcfone`

> You'll need to manually stop to the application with `Ctrl+C`

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
> **Note**: The environment variables declared above represent a minimum required to authorize cf-butler to collect data from [PCFOne](https://login.run.pcfone.io/login).  Consult the `*.json` secret file [samples](samples) for your specific needs to vary behavior and features. 

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


## How to deploy to VMware Tanzu Application Service

### with Username and password authorization

The following instructions explain how to get started when `cf.tokenProvider` is set to `userpass`

Authenticate to a foundation using the API endpoint.

> E.g., login to [IBM Cloud](https://cloud.ibm.com/login)

```
cf login -a api.us-south.cf.cloud.ibm.com -u <username> -p <password>
```

### with SSO authorization

The following instructions explain how to get started when `cf.tokenProvider` is set to `sso`

Authenticate to a foundation using the API endpoint

> E.g., login to [PCF One](https://login.run.pcfone.io)

```
cf login -a https://api.run.pcfone.io -sso
```

Visit the link in the password prompt to retrieve a temporary passcode, then complete the login process

> E.g., `https://login.run.pcfone.io/passcode`)

Inspect the contents of `~/.cf/config.json` and copy the value of `RefreshToken`.

Paste the value as the value for `CF_REFRESH-TOKEN` in your `config/secrets.json`

```
{
  "CF_TOKEN-PROVIDER": "sso",
  "CF_API-HOST": "xxxxx",
  "CF_REFRESH-TOKEN": "xxxxx",
}
```

### using scripts

Please review the [manifest.yml](manifest.yml) before deploying.

Deploy the app (w/ a user-provided service instance vending secrets)

```
./scripts/deploy.sh
```

Deploy the app (w/ a Credhub service instance vending secrets)

```
./scripts/deploy.sh --with-credhub
```

Shutdown and destroy the app and service instances

```
./scripts/destroy.sh
```

> Note: If you are seeing [OutOfMemory exceptions](https://dzone.com/articles/troubleshooting-problems-with-native-off-heap-memo) shortly after startup you may need to [cf scale](https://docs.run.tanzu.vmware.com/devguide/deploy-apps/cf-scale.html#vertical) the available memory for large foundations.

## Endpoints

These REST endpoints have been exposed for reporting and administrative purposes.

### Operations Manager

These endpoints are only available when the `om.enabled` property is set to `true`, `om.apiHost` has been set to a valid URL, along with requisite credentials properties based on a supported token grant type.  Mimics a reduced set of the [Operations Manager API](https://docs.pivotal.io/vmware-tanzucf/2-6/opsman-api/).

```
GET /products/deployed
```
> List of all tiles installed on foundation.

```
GET /products/stemcell/assignments
```
> Lists all stemcells associated with installed tiles (includes staged and available stemcell versions).

```
GET /products/stemcell/associations
```
> Lists all stemcells associated with installed tiles (includes staged and available stemcell versions).
> Only available from Operations Manager 2.7.0 onward.

```
GET /products/om/info
```
> Returns the current version of the Operations Manager instance

```
GET /products/metrics
```
> Returns release metric information for installed products (includes buildpacks, stemcells and tiles).  Note: stemcell release metrics are only available from foundations administered with Ops Manager 2.6 or later.

Sample output

```
{
    "product-metrics": [
        {
            "currently-installed-release-date": null,
            "currently-installed-version": "1.6.0-build.41",
            "end-of-life": false,
            "end-of-support-date": null,
            "latest-available-release-date": "2019-01-10",
            "latest-available-version": "1.6.0",
            "name": "apm",
            "pre-release": true,
            "days-behind-latest-available-version": null,
            "type": "tile"
        },
        {
            "currently-installed-release-date": "2019-06-19",
            "currently-installed-version": "2.7.0",
            "end-of-life": false,
            "end-of-support-date": "2020-03-31",
            "latest-available-release-date": "2019-07-15",
            "latest-available-version": "2.6.2",
            "name": "elastic-runtime",
            "pre-release": false,
            "days-behind-latest-available-version": 26,
            "type": "tile"
        },
        {
            "currently-installed-release-date": "2019-05-31",
            "currently-installed-version": "3.0.2",
            "end-of-life": false,
            "end-of-support-date": "2020-02-29",
            "latest-available-release-date": "2019-05-31",
            "latest-available-version": "3.0.2",
            "name": "p-spring-cloud-services",
            "pre-release": false,
            "days-behind-latest-available-version": 0,
            "type": "tile"
        },
        {
            "currently-installed-release-date": "2019-07-02",
            "currently-installed-version": "1.3.3",
            "end-of-life": false,
            "end-of-support-date": "2020-07-02",
            "latest-available-release-date": "2019-07-02",
            "latest-available-version": "1.3.3",
            "name": "credhub-service-broker",
            "pre-release": false,
            "days-behind-latest-available-version": 0,
            "type": "tile"
        },
        {
            "currently-installed-release-date": "2019-04-25",
            "currently-installed-version": "1.7.38",
            "days-behind-latest-available-version": 82,
            "end-of-life": false,
            "end-of-support-date": null,
            "latest-available-release-date": "2019-07-16",
            "latest-available-version": "1.7.42",
            "name": "ruby-buildpack",
            "pre-release": false,
            "type": "buildpack"
        },
        {
            "currently-installed-release-date": "2019-04-24",
            "currently-installed-version": "1.6.32",
            "days-behind-latest-available-version": 84,
            "end-of-life": false,
            "end-of-support-date": null,
            "latest-available-release-date": "2019-07-17",
            "latest-available-version": "1.6.36",
            "name": "python-buildpack",
            "pre-release": false,
            "type": "buildpack"
        },
        {
            "name": "apm:1.6.0-build.41:ubuntu-xenial",
            "type": "stemcell",
            "currently-installed-release-date": "2019-06-24",
            "currently-installed-version": "170.93",
            "latest-available-release-date": "2019-07-16",
            "latest-available-version": "170.109",
            "end-of-support-date": null,
            "days-behind-latest-available-version": 22,
            "end-of-life": false,
            "pre-release": false
        },
    ]
}
```


### VMware Tanzu Network

These endpoints are only available when the `pivnet.enabled` property is set to `true`. A valid `pivnet.apiToken` property must also have been defined.  Mimics a reduced set of the [VMware Tanzu Network API](https://network.pivotal.io/docs/api).

```
GET /store/product/catalog
```
> Retrieves a list of all products from VMware Tanzu Network (includes buildpacks, stemcells and tiles)

```
GET /store/product/releases?q=latest
```
> Returns a list of the latest available releases for all products on VMware Tanzu Network (includes buildpacks, stemcells and tiles)

```
GET /store/product/releases?q=all
```
> Returns a list of all available releases for all products on VMware Tanzu Network (includes buildpacks, stemcells and tiles)

```
GET /store/product/releases?q=recent
```
> Returns a list of releases released within the last 7 days for all products on VMware Tanzu Network (includes buildpacks, stemcells and tiles)


### Events

Based off the [Events API](https://apidocs.cloudfoundry.org/287/events/list_all_events.html) and exposed only when the `spring.profiles.active` includes the `on-demand` profile.

```
GET /events/{id}
```
> Returns the last 10 events for an actee guid (e.g., application, service instance)

```
GET /events/{id}?numberOfEvents={n}
```
> Returns n events (up to a maximum of 250) for an actee guid (e.g., application, service instance)

```
GET /events/{id}?types[]={type1,type2,...,typen}
```
> Returns matching events for an actee guid (e.g., application, service instance).  An comma-separated array of valid event types must be specified.


### Metadata

Metadata is comprised of [labels](https://v3-apidocs.cloudfoundry.org/version/3.76.0/index.html#labels-and-selectors) and/or [annotations](https://v3-apidocs.cloudfoundry.org/version/3.76.0/index.html#annotations).  Labels work with selectors to subsequently help you lookup resources.

```
GET /metadata/{type}/{id}
```
> Return resource with id, created date, last updated date, and associated metadata; where `{type}` is the id field of a [ResourceType](https://github.com/cf-toolsuite/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ResourceType.java) enum and `{id}` is the guid of a resource.

```
PATCH /metadata/{type}/{id}
```
> Update metadata associated with a resource id

Sample body

```
{
    "metadata": {
        "labels": {
        	"cost-center": "11009872"
        },
        "annotations": {
        	"region": "us-west2"
        }
    }
}
```

### Snapshot

```
GET /snapshot/organizations
```
> Lists organizations

```
GET /snapshot/organizations/count
```
> Counts the number of organizations on a foundation

```
GET /snapshot/spaces
```
> Lists spaces

```
GET /snapshot/spaces/count
```
> Counts the number of spaces on a foundation

```
GET /snapshot/spaces/users
```
> Provides details and light metrics for users by role within all organizations and spaces on a foundation

Sample output
```
[
  {
    organization: "Northwest",
    space: "akarode",
    auditors: [ ],
    developers: [
      "wlund@tanzu.vmware.com",
      "akarode@tanzu.vmware.com"
    ],
    managers: [
      "wlund@tanzu.vmware.com",
      "akarode@tanzu.vmware.com"
    ],
    users: [
      "wlund@tanzu.vmware.com",
      "akarode@tanzu.vmware.com"
    ],
    user-count: 2,
  },
  {
    organization: "Northwest",
    space: "arao",
    auditors: [ ],
    developers: [
      "arao@tanzu.vmware.com"
    ],
    managers: [
      "arao@tanzu.vmware.com"
    ],
    users: [
      "arao@tanzu.vmware.com"
    ],
    user-count: 1
  },
...
```
> `users` is the unique subset of all users from each role in the organization/space

```
GET /snapshot/spaces/users/{name}
```
> Provides a listing of all organizations/spaces associated with a single user account

```
GET /snapshot/{organization}/{space}/users
```
> Provides details and light metrics for users by role within a targeted organization and space

```
GET /snapshot/users
```
> Lists all unique user accounts on a foundation

```
GET /snapshot/users/count
```
> Counts the number of user accounts on a foundation

```
GET /snapshot/summary
```
> Provides summary metrics for applications, service instances, and users on a foundation

> **Note**: this summary report does not take the place of an official foundation Accounting Report. The Accounting Report is focussed on calculating aggregates (on a monthly basis) such as: (a) the total hours of application instance usage, (b) the largest # of application instances running (a.k.a. maximum concurrent application instances), c) the total hours of service instance usage and (d) the largest # of service instances running (a.k.a. maximum concurrent service instances).

Sample output
```
{
  "application-counts": {
    "by-organization": {
      "Northwest": 35
    },
    "by-buildpack": {
      "java": 28,
      "nodejs": 2,
      "unknown": 5
    },
    "by-stack": {
      "cflinuxfs2": 20,
      "cflinuxfs3": 15
    },
    "by-dockerimage": {
      "--": 0
    },
    "by-status": {
      "stopped": 15,
      "started": 20
    },
    "total-applications": 35,
    "total-running-application-instances": 21,
    "total-stopped-application-instances": 18,
    "total-crashed-application-instances": 3,
    "total-application-instances": 42,
    "velocity": {
      "between-two-days-and-one-week": 6,
      "between-one-week-and-two-weeks": 0,
      "between-one-day-and-two-days": 3,
      "between-one-month-and-three-months": 5,
      "between-three-months-and-six-months": 4,
      "between-two-weeks-and-one-month": 1,
      "in-last-day": 0,
      "between-six-months-and-one-year": 10,
      "beyond-one-year": 6
    }
  },
  "service-instance-counts": {
    "by-organization": {
    "Northwest": 37
    },
    "by-service": {
      "rediscloud": 2,
      "elephantsql": 4,
      "mlab": 2,
      "p-service-registry": 2,
      "cleardb": 10,
      "p-config-server": 2,
      "user-provided": 9,
      "app-autoscaler": 2,
      "cloudamqp": 4
    },
    "by-service-and-plan": {
      "cleardb/spark": 10,
      "mlab/sandbox": 2,
      "rediscloud/30mb": 2,
      "p-service-registry/trial": 2,
      "elephantsql/turtle": 4,
      "p-config-server/trial": 2,
      "cloudamqp/lemur": 4,
      "app-autoscaler/standard": 2
    },
    "total-service-instances": 37,
    "velocity": {
      "between-two-days-and-one-week": 4,
      "between-one-week-and-two-weeks": 1,
      "between-one-day-and-two-days": 2,
      "between-one-month-and-three-months": 3,
      "between-three-months-and-six-months": 0,
      "between-two-weeks-and-one-month": 1,
      "in-last-day": 0,
      "between-six-months-and-one-year": 5,
      "beyond-one-year": 8
    }
  },
  "user-counts": {
    "by-organization": {
      "zoo-labs": 1,
      "Northwest": 14
    },
    "total-users": 14
  }
}
```

```
GET /snapshot/detail
```
> Provides lists of all applications and service instances (by organization and space) and accounts (split into sets of user and service names) on the foundation

> **Note**: this detail report does not take the place of an official foundation Accounting Report. However, it does provide a much more detailed snapshot of all the applications that were currently running at the time of collection.

> As of [PR-391](https://github.com/cf-toolsuite/cf-butler/pull/391), buildpack name and version are based upon the current application droplet.  If you observe that both the buildpack name and version are null, then you may need to `cf restage` your application (e.g., when the last `cf push` exceeds 30 days).

```
GET /snapshot/detail/ai
```
> Provides lists of all applications in comma-separated value format

Sample output

```
Application inventory detail from api.sys.sangabriel.cf-app.com collected 2023-06-07T00:55:34 and generated 2023-06-07T01:35:11.606166653.


organization,space,application id,application name,buildpack,buildpack version,image,stack,running instances,total instances,memory used (in gb),memory quota (in gb),disk used (in gb),disk quota (in gb),urls,last pushed,last event,last event actor,last event time,requested state,latest buildpack release type,latest buildpack release date,latest buildpack version,latest buildpack Url
"arul","dev","ff1f2147-079c-4f58-bbe7-ad0fb905a2e8","pcfdemo",,,,"cflinuxfs3","0","1","0.0","0.0","0.0","0.0","pcfdemo.apps.sangabriel.cf-app.com",,"audit.app.update","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-05-12T16:10:12","stopped",,,,
"arul","dev","655aab3a-8b77-42ce-a87b-aa5848cc9d7d","rabbitmq-example-app",,,,"cflinuxfs3","0","2","0.0","0.0","0.0","0.0","rabbitmq-example-app.apps.sangabriel.cf-app.com","2023-05-12T16:10:14","audit.app.build.create","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-05-12T16:10:27","stopped",,,,
"arul","dev","b814712d-03e9-44b2-ac28-1946cbdbc82c","spring-music","java","v4.54",,"cflinuxfs3","1","1","0.21608664747327566","1.0","0.16757965087890625","1.0","spring-music-noisy-kookaburra-eg.apps.sangabriel.cf-app.com","2023-05-12T16:10:14","audit.app.restart","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-05-12T16:11:28","started",,,,
"arul","prod","3a414a89-388c-4625-9fbf-0cd2b889345a","rabbitmq",,,,"cflinuxfs3","0","2","0.0","0.0","0.0","0.0","rabbitmq.apps.sangabriel.cf-app.com","2023-05-12T16:10:14","audit.app.build.create","01ecf2c7-f4dd-4ca5-8dfd-30df64f09918","2023-05-12T16:10:25","stopped",,,,
"credhub-service-broker-org","credhub-service-broker-space","b95adbde-2597-4150-b048-8be45796cab6","credhub-broker-1.5.1","binary","1.1.3",,"cflinuxfs3","1","1","0.016004773788154125","0.25","0.009128570556640625","1.0","credhub-broker.apps.sangabriel.cf-app.com","2023-05-12T15:31:52","audit.app.droplet.create","82dcc4bb-ef83-4db8-b05a-a0e2b88e67e3","2023-05-12T15:32:21","started",,,,
"dev","observability","be785d20-fd7e-4674-bb7f-8b742b40e1d4","cf-butler","java","v4.54",,"cflinuxfs3","1","1","0.1790512539446354","1.0","0.211700439453125","1.0","cf-butler.apps.sangabriel.cf-app.com","2023-06-07T00:54:18","audit.app.restart","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-06-07T00:55:07","started",,,,
"dev","observability","2858eaba-5c18-4916-849b-7676c6bb33c5","cf-hoover","java","v4.54",,"cflinuxfs3","1","1","0.40730543807148933","2.0","0.2159271240234375","1.0","cf-hoover-forgiving-wombat-sr.apps.sangabriel.cf-app.com","2023-04-14T12:54:07",,,,"started",,,,
"dev","observability","f1e7eb6c-7d54-4968-b6e4-3f9f8fade059","cf-hoover-ui","java","v4.54",,"cflinuxfs3","1","1","0.5119553785771132","2.0","0.2413177490234375","1.0","cf-hoover-ui-chatty-chimpanzee-jf.apps.sangabriel.cf-app.com","2023-04-14T14:30:03",,,,"started",,,,
"dev","sample-apps","89b4ae55-705a-4572-9890-46d5171a613c","nicky-butler","java","v4.54",,"cflinuxfs3","0","1","0.0","0.0","0.0","0.0","nicky-butler.apps.sangabriel.cf-app.com","2023-04-14T18:44:03","audit.app.stop","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-05-16T02:45:57","stopped",,,,
"dev","tap","1fdc8b02-99a7-4741-8c9d-7df4363a7f4d","tas-java-web-app",,,"dev.registry.pivotal.io/warroyo/supply-chain/tas-java-web-app-dev-tap@sha256:0943939b3ca1bcb6527ad39bf766d84b9be4455a14a0b67ab7c4b6f4840750e1","cflinuxfs3","2","2","0.352079289034009","1.998046875","0.5242393091320992","1.0","tas-java-web-app.apps.sangabriel.cf-app.com","2023-05-10T18:36:44","audit.app.update","9602fa7e-d66f-4a0e-b78c-b191106b79c4","2023-05-10T18:40:46","started",,,,
"p-spring-cloud-services","249f77f1-63a9-41c2-a26a-ad848df3fcba","3e4c55ca-ed5f-4e97-86c2-25d38626b90d","config-server","java","v4.54",,"cflinuxfs3","1","1","0.24609375","1.0","0.15219497680664062","1.0","config-server-249f77f1-63a9-41c2-a26a-ad848df3fcba.apps.sangabriel.cf-app.com","2023-04-14T12:54:45",,,,"started",,,,
"p-spring-cloud-services","63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed","893f0f47-6eed-4bd0-bb83-5472b7ea4c07","service-registry","java","v4.54",,"cflinuxfs3","1","1","0.2760823564603925","1.0","0.17532730102539062","1.0","service-registry-63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed.apps.sangabriel.cf-app.com","2023-04-14T12:54:47",,,,"started",,,,
"system","autoscaling","d7396dab-0a12-4e68-a78f-c6008d5051a7","autoscale","binary","1.1.3",,"cflinuxfs3","3","3","0.0546162910759449","0.25","0.052013397216796875","1.0","autoscale.sys.sangabriel.cf-app.com","2023-04-13T15:06:22",,,,"started",,,,
"system","autoscaling","c5aa1354-6dd4-41f5-aa7b-939a19029532","autoscale-api","java","v4.54",,"cflinuxfs3","1","1","0.2501183710992336","1.0","0.188018798828125","1.0","autoscale.sys.sangabriel.cf-app.com/api/v2","2023-04-13T15:06:51",,,,"started",,,,
"system","notifications-with-ui","806444bb-3da7-468e-9da6-d5bde5b56fd7","notifications-ui","binary","1.1.3",,"cflinuxfs3","2","2","0.021087645553052425","0.0625","0.02587890625","1.0","notifications-ui.sys.sangabriel.cf-app.com","2023-04-13T15:05:45",,,,"started",,,,
"system","offline-docs","3b30b7d8-d170-41c7-b4e3-20a0e4eb369a","offline-docs","ruby","1.9.2",,"cflinuxfs3","1","1","0.09670342318713665","0.25","0.21299362182617188","1.0","offline-docs.apps.sangabriel.cf-app.com","2023-04-13T15:01:02",,,,"started",,,,
"system","p-dataflow","ed513df1-a946-45b8-8975-2db788f0bbec","p-dataflow-1.13.0","java","v4.54",,"cflinuxfs3","1","1","0.6273137014359236","2.0","0.44135284423828125","1.0","p-dataflow.apps.sangabriel.cf-app.com","2023-04-27T18:13:46",,,,"started",,,,
"system","system","ecd6f8da-38ef-4714-9d84-cee2cb5f13d4","app-usage-scheduler","ruby","1.9.2",,"cflinuxfs3","1","1","0.0999792842194438","1.0","0.1661376953125","1.0",,"2023-04-13T14:56:21",,,,"started",,,,
"system","system","9bd4378d-c97f-46cd-bcf9-e99fcd56297d","app-usage-server","ruby","1.9.2",,"cflinuxfs3","2","2","0.5762754492461681","1.0","0.33228302001953125","1.0","app-usage.sys.sangabriel.cf-app.com","2023-04-13T14:56:21",,,,"started",,,,
"system","system","fefc5be6-90ee-4b87-8815-1c13af429dd9","app-usage-worker","ruby","1.9.2",,"cflinuxfs3","1","1","0.11408127937465906","2.0","0.1661376953125","1.0",,"2023-04-13T14:56:20",,,,"started",,,,
"system","system","31c7e146-0949-40a4-b57f-d11d403916e1","apps-manager-js-green","staticfile","1.6.0",,"cflinuxfs3","6","6","0.10966186318546534","0.125","0.78497314453125","1.0","apps.sys.sangabriel.cf-app.com","2023-04-13T15:02:38",,,,"started",,,,
"system","system","b610eb6f-9c9f-4906-872d-d12021232755","p-invitations-green","nodejs","1.8.6",,"cflinuxfs3","2","2","0.12076483760029078","0.25","0.36865997314453125","1.0","p-invitations.sys.sangabriel.cf-app.com","2023-04-13T15:02:32",,,,"started",,,,
"system","system","586a6e4e-b6df-4fe5-88a3-eb9b7c667144","search-server-green","nodejs","1.8.6",,"cflinuxfs3","2","2","0.1306796595454216","0.25","0.3582916259765625","1.0","search-server.sys.sangabriel.cf-app.com","2023-04-13T15:02:29",,,,"started",,,,
"zoo-labs","demo","35eb5fdf-46a3-4d77-bfe6-dd29878a1553","primes","java","v4.54",,"cflinuxfs4","2","2","0.3480446543544531","1.0","0.35300445556640625","1.0","primes-bogus-hedgehog-kx.apps.sangabriel.cf-app.com","2023-05-25T17:59:41","audit.app.environment.show","bcbc230c-2ecc-4dc9-91de-6b49776ad403","2023-06-06T20:15:19","started",,,,
```

```
GET /snapshot/detail/si
```
> Provides a list of all service instances in comma-separated value format

Sample output

```
Service inventory detail from api.sys.sangabriel.cf-app.com collected 2023-06-07T00:55:34 and generated 2023-06-07T01:35:51.430427874.


organization,space,service instance id,name,service,description,plan,type,bound applications,last operation,last updated,dashboard url,requested state
"dev","observability","efe26b17-0722-4b9d-8160-2b804adf4bbc","cf-butler-secrets",,,,"user_provided_service_instance","cf-butler",,,,
"dev","observability","249f77f1-63a9-41c2-a26a-ad848df3fcba","cf-hoover-config","p.config-server","Service to provide configuration to applications at runtime.","standard","managed_service_instance","cf-hoover","create","2023-04-14T12:56:27","https://config-server-249f77f1-63a9-41c2-a26a-ad848df3fcba.apps.sangabriel.cf-app.com/dashboard","succeeded"
"dev","observability","731a52c3-f189-47cb-b936-0442ad63da26","cf-butler-backend","p.mysql","Dedicated instances of MySQL","db-small-80","managed_service_instance","cf-butler","create","2023-04-13T20:53:47",,"succeeded"
"dev","observability","63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed","hooverRegistry","p.service-registry","Deploys Eureka server as a service registry for application clients.","standard","managed_service_instance","cf-hoover,cf-hoover-ui","create","2023-04-14T12:56:33","https://service-registry-63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed.apps.sangabriel.cf-app.com/dashboard","succeeded"
"dev","sample-apps","69ee91b3-7493-45bd-b0bb-d9dc3e04a0bd","cf-butler-backend","p.mysql","Dedicated instances of MySQL","db-small-80","managed_service_instance","nicky-butler","create","2023-04-14T15:20:18",,"succeeded"
"p-spring-cloud-services","249f77f1-63a9-41c2-a26a-ad848df3fcba","5f18839e-8425-4e58-925b-4f4cab17a378","mirror-svc","p.mirror-service","Spring Cloud Config Server git mirror service. This is an internal system service and should not be created directly by end users.","standard","managed_service_instance","config-server","create","2023-04-14T12:54:31",,"succeeded"
"system","system","c004a59c-c604-430a-b19a-345f1f3653c2","structured-format-json",,,,"user_provided_service_instance","app-usage-worker",,,,
```

```
GET /snapshot/detail/relations
```
> Provides a list of all application to service instance relationships in comma-separated value format

Sample output

```
Application relationships from api.sys.sangabriel.cf-app.com collected 2023-06-07T00:55:34 and generated 2023-06-07T01:36:41.935819779.


organization,space,application id,application name,service instance id,service name,service offering,service plan,service type
"dev","observability","be785d20-fd7e-4674-bb7f-8b742b40e1d4","cf-butler","efe26b17-0722-4b9d-8160-2b804adf4bbc","cf-butler-secrets",,,"user_provided_service_instance"
"dev","observability","be785d20-fd7e-4674-bb7f-8b742b40e1d4","cf-butler","731a52c3-f189-47cb-b936-0442ad63da26","cf-butler-backend","p.mysql","db-small-80","managed_service_instance"
"dev","observability","2858eaba-5c18-4916-849b-7676c6bb33c5","cf-hoover","63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed","hooverRegistry","p.service-registry","standard","managed_service_instance"
"dev","observability","2858eaba-5c18-4916-849b-7676c6bb33c5","cf-hoover","249f77f1-63a9-41c2-a26a-ad848df3fcba","cf-hoover-config","p.config-server","standard","managed_service_instance"
"dev","observability","f1e7eb6c-7d54-4968-b6e4-3f9f8fade059","cf-hoover-ui","63df5b0a-b4a8-4be1-8aad-c54dab4cb7ed","hooverRegistry","p.service-registry","standard","managed_service_instance"
"dev","sample-apps","89b4ae55-705a-4572-9890-46d5171a613c","nicky-butler","69ee91b3-7493-45bd-b0bb-d9dc3e04a0bd","cf-butler-backend","p.mysql","db-small-80","managed_service_instance"
"p-spring-cloud-services","249f77f1-63a9-41c2-a26a-ad848df3fcba","3e4c55ca-ed5f-4e97-86c2-25d38626b90d","config-server","5f18839e-8425-4e58-925b-4f4cab17a378","mirror-svc","p.mirror-service","standard","managed_service_instance"
"system","system","fefc5be6-90ee-4b87-8815-1c13af429dd9","app-usage-worker","c004a59c-c604-430a-b19a-345f1f3653c2","structured-format-json",,,"user_provided_service_instance"
```

```
GET /snapshot/detail/dormant/{daysSinceLastUpdate}
```
> Provides a list of dormant workloads. A workload is either an application or service instance.  An application is considered dormant when the last retained event transpired `daysSinceLastUpdate` or longer from the time of request.  A service instance is considered dormant when the last retained event transpired `daysSinceLastUpdate` or longer from the time of request.  Note: audit events are [retained](https://docs.cloudfoundry.org/running/managing-cf/audit-events.html#considerations) for up to 31 days.

```
GET /snapshot/detail/legacy?stacks={stacks}
```
> Returns a list of all applications that have been deployed on legacy stacks. Replace `{stacks}` request parameter-value above with a comma-separated list of legacy stacks like `windows2012R2,cflinuxfs2`.

```
GET /snapshot/detail/legacy?service-offerings={service-offerings}
```
> Returns a list of all service instances matching a product slug name as defined in the comma-separated list. Replace `{service-offerings}` request parameter-value above with a comma-separated list of legacy service offerings like `p-config-server, p-service-registry, p-mysql`.

```
GET /snapshot/detail/users
```
> Provides a list of all space users (ignoring role) in comma-separated value format by organization and space, where multiple users in each space are comma-separated. Service accounts are filtered.

Sample output

```
User accounts from api.sys.cf.zoo.labs.foo.org generated 2019-05-17T00:19:45.932764.


organization,space,user accounts
"mvptime","default","cphillipson@tanzu.vmware.com,bruce.lee@kungfulegends.com,vmanoharan@tanzu.vmware.com"
"planespotter","default","stan.lee@marvel.com,vmanoharan@tanzu.vmware.com"
```

```
GET /snapshot/demographics
```
> Yields organization, space user account, and service account totals on the foundation

Sample output

```
{
  "total-organizations": 4,
  "total-spaces": 11,
  "total-user-accounts": 3,
  "total-service-accounts": 3
}
```

#### Java Applications

```
GET /download/pomfiles
```
> Downloads a .tar.gz file that contains pom.xml files for all Java applications that had had their artifacts built and packaged by Maven. Note: this endpoint is only available when the `java.artifact.reader.impl` property is set to `pom`

The tarball contents will be structured as follows: `{organization}/{space}/{application-name}/pom.xml`.  A stakeholder interested in understanding the health and well-being of the subset of their applications leveraging the Spring Framework and/or Spring Boot, could unpack the tarball, visit each directory and execute `mvn depdendencies:tree | grep -E '(org.springframework|io.micrometer)' > spring-dependencies.txt`, then submit each dependencies file to the online [Spring Health Assessment](https://tanzu.vmware.com/spring-health-assessment) reporting service.

You can use this companion [script](scripts/process-java-app-dependencies-tarball.sh) to consume the .tar.gz file.  It will unpack the .tar.gz file, visit each directory where a pom.xml file resides and generate a spring-dependencies.txt file.  Certainly useful when you have a large number of applications to obtain dependencies for.


```
GET /snapshot/detail/ai/spring
```
> Return a filtered list of applications that are utilizing Spring dependencies

Sample output

```
[
    {
        "appId": "f381a7dd-42df-4c57-9d30-37f8ade12012",
        "appName": "cf-butler",
        "dropletId": "797b9bfd-0de2-48a9-b22e-90d7a61fd988",
        "organization": "observability",
        "space": "demo",
        "springDependencies": "org.springframework.boot:spring-boot-starter-parent:3.2.2, org.springframework.cloud:spring-cloud-dependencies:2023.0.0"
    }
]
```

```
GET /snapshot/summary/ai/spring
```
> Calculates the frequency of occurrence for each Spring dependency found

Sample output

```
{
    "org.springframework.boot:spring-boot-starter-parent:3.2.2": 10,
    "org.springframework.cloud:spring-cloud-dependencies:2023.0.0": 7
}
```


### Accounting

> Note: `/accounting/**` endpoints below require a user with `cloud_controller.admin` or `usage_service.audit` scope.  See [Creating and Managing Users with the UAA CLI (UAAC)](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/4.0/tas-for-vms/uaa-user-management.html).

```
GET /accounting/applications
```
> Produces a system-wide account report of [application usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#app-usage)

> **Note**: Report excludes application instances in the `system` org

```
GET /accounting/services
```
> Produces a system-wide account report of [service usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#service-usage)

> **Note**: Report excludes user-provided service instances

```
GET /accounting/tasks
```
> Produces a system-wide account report of [task usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#task-usage)

```
GET /accounting/applications/{orgName}/{startDate}/{endDate}
```
> Produces an [application usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#org-app-usage) constrained to single organization and time period

```
GET /accounting/services/{orgName}/{startDate}/{endDate}
```
> Produces a [service usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#service-usage-9) constrained to single organization and time period

```
GET /accounting/tasks/{orgName}/{startDate}/{endDate}
```
> Produces a [task usage](https://docs.vmware.com/en/VMware-Tanzu-Application-Service/3.0/tas-for-vms/accounting-report.html#task-usage-8) constrained to single organization and time period


### Policies

```
POST /policies

{
  "application-policies": [
    {
      "description": "Remove stopped applications that are older than some date/time from now and restricted to whitelisted organizations",
      "operation": "delete",
      "state": "stopped",
      "options": {
        "from-datime": "2019-07-01T12:30:00",
        "delete-services": true
      },
      "organization-whitelist": [
        "zoo-labs"
      ]
    },
    {
      "description": "Scale running applications restricted to whitelisted organizations",
      "operation": "scale-instances",
      "state": "started",
      "options": {
        "instances-from": 1,
        "instances-to": 2
      },
      "organization-whitelist": [
        "zoo-labs"
      ]
    },
    {
      "description": "Change the stack for applications restricted to whitelisted organizations",
      "operation": "change-stack",
      "state": "started",
      "options": {
        "stack-from": "cflinuxfs2",
        "stack-to": "cflinuxfs3"
      },
      "organization-whitelist": [
        "zoo-labs",
        "jujubees",
        "full-beaker"
      ]
    }
  ],
  "service-instance-policies": [
    {
      "description": "Remove orphaned services that are older than some duration from now and restricted to whitelisted organizations",
      "operation": "delete",
      "options": {
        "from-duration": "P1D"
      },
      "organization-whitelist": [
        "zoo-labs"
      ]
    }
  ],
  "endpoint-policies": [
    {
      "description": "Sample endpoint policy that will fetch data from available /snapshot endpoints.",
      "endpoints": [
        "/snapshot/summary",
        "/snapshot/detail"
      ],
      "email-notification-template": {
        "from": "admin@nowhere.me",
        "to": [ "captainmarvel@theuniverse.io" ],
        "subject": "Endpoint Policy Sample Report",
        "body": "Results are herewith attached for your consideration."
      }
    }
  ],
  "query-policies": [
    {
      "description": "Query policy that will run two queries and email the results as per the template configuration.",
      "queries": [
        {
          "name": "docker-images",
          "description": "Find all running Docker image based containers",
          "sql": "select * from application_detail where running_instances > 0 and requested_state = 'started' and image is not null"
        },
        {
          "name": "all-apps-pushed-and-still-running-in-the-last-week",
          "description": "Find all running applications pushed in the last week not including the system organization",
          "sql": "select * from application_detail where running_instances > 0 and requested_state = 'started' and week(last_pushed) = week(current_date) -1 AND year(last_pushed) = year(current_date) and organization not in ('system')"
        }
      ],
      "email-notification-template": {
        "from": "admin@nowhere.me",
        "to": [
          "drwho@tardis.io"
        ],
        "subject": "Query Policy Sample Report",
        "body": "Results are herewith attached for your consideration."
      }
    }
  ],
  "hygiene-policies": [
    {
      "days-since-last-update": 14,
      "include-applications": true,
      "include-service-instances": true,
      "operator-email-template": {
        "from": "admin@nowhere.me",
        "to": [
          "cphillipson@tanzu.vmware.com"
        ],
        "subject": "Hygiene Policy Platform Operator Report",
        "body": "These are the dormant workloads in a single organization"
      },
      "notifyee-email-template": {
        "from": "admin@nowhere.me",
        "subject": "Hygiene Policy User Workloads Report",
        "body": "You may want to revisit whether or not these workloads are useful.  Please take a moment to either stop and/or delete them if they aren't."
      },
      "organization-whitelist": [
        "blast-radius"
      ]
    }
  ],
  "legacy-policies": [
    {
      "notifyee-email-template": {
        "body": "<h3>These applications are deployed to a legacy stack</h3><p>To avoid repeated notification:</p><ul><li>for each application please execute a cf push and update the stack to a modern alternative or cf delete</li></ul><p>depending on whether or not you want to keep the workload running.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report"
      },
      "operator-email-template": {
        "body": "<h3>These applications are deployed to a legacy stack</h3><p>Results are herewith attached for your consideration.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report",
        "to": [
          "cphillipson@tanzu.vmware.com"
        ]
      },
      "organization-whitelist": [],
      "stacks": [
        "cflinuxfs2",
        "windows2012R2"
      ]
    },
    {
      "notifyee-email-template": {
        "body": "<h3>These service instances are legacy service offerings</h3><p>To avoid repeated notification:</p><ul><li>for each application please bind to a modern alternative or cf delete</li></ul><p>depending on whether or not you want to keep the workload running.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report"
      },
      "operator-email-template": {
        "body": "<h3>These service instances are legacy service offerings</h3><p>Results are herewith attached for your consideration.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report",
        "to": [
          "cphillipson@tanzu.vmware.com"
        ]
      },
      "organization-whitelist": [],
      "service-offerings": [
        "p-config-server"
      ]
    },
    {
    "resource-notification-policies": [
        {
            "resource-email-template": {
                "from": "admin@pcf.demo.ironleg.me",
                "subject": "Platform Updates",
                "body": "Please take a moment to review the platform updates and share it with your Org users"
            },
            "resource-email-metadata": {
                "resource": "organizations",
                "labels": [
                            "PrimaryOwner",
                            "SecondaryOwner"
                          ],
                "email-domain": "tanzu.vmware.com"
            }
            "resource-whitelist": [
                "p-config-server"
            ]
            "resource-blacklist": [
                "pivot-sample-org"
            ]
        }
    ]
}
  ]
}
```
> Establish policies to delete and scale applications, delete service instances, and query for anything from schema. This endpoint is only available when `cf.policies.git.uri` is not set.

> Consult the [java.time.Duration](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-) javadoc for other examples of what you can specify when setting values for `from-duration` properties above.

```
GET /policies
```
> List current policies

```
DELETE /policies
```
> Delete all established policies. This endpoint is only available when `cf.policies.git.uri` is set to an empty string.

```
GET /policies/application/{id}
```
> Obtain application policy details by id

```
GET /policies/serviceInstance/{id}
```
> Obtain service instance policy details by id

```
GET /policies/query/{id}
```
> Obtain query policy details by id

```
GET /policies/hygiene/{id}
```
> Obtain hygiene policy details by id

```
GET /policies/legacy/{id}
```
> Obtain legacy policy details by id

```
DELETE /policies/application/{id}
```
> Delete an application policy by its id. This endpoint is only available when `cf.policies.git.uri` is not set.

```
DELETE /policies/serviceInstance/{id}
```
> Delete a service instance policy by its id. This endpoint is only available when `cf.policies.git.uri` is not set.

```
DELETE /policies/query/{id}
```
> Delete a query policy by its id. This endpoint is only available when `cf.policies.git.uri` is not set.

```
DELETE /policies/hygiene/{id}
```
> Delete a hygiene policy by its id. This endpoint is only available when `cf.policies.git.uri` is not set.

```
DELETE /policies/legacy/{id}
```
> Delete a legacy policy by its id. This endpoint is only available when `cf.policies.git.uri` is not set.

```
POST /policies/refresh
```
> Refreshes policies loaded from an external Git repo. This endpoint is only available when `cf.policies.provider` is set to `git`.

```
GET /policies/report
```
> Produces `text/plain` historical output detailing what policies had an effect on applications and service instances.  (Does not track execution of query policies).

```
GET /policies/report?start={startDate}&end={endDate}
```
> Produces `text/plain` historical output detailing what policies had an effect on applications and service instances constrained by date range.  `{startDate}` must be before `{endDate}`.  Both parameters are [LocalDate](https://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html).  (Does not track execution of query policies).


### Collection and Execution

These endpoints are exposed only when `spring.profiles.active` includes the `on-demand` profile.  These are expensive operations meant to be triggered on a cron schedule rather than manually invoked.

```
POST /collect
```
> On-demand trigger for collecting snapshot data

```
POST /policies/execute
```
> On-demand trigger for executing all registered policies.  You'll absolutely want to trigger the `/collect` endpoint and wait for it to complete before triggering this endpoint.

## Credits

* [Oleh Dokuka](https://github.com/OlegDokuka) for writing [Hands-on Reactive Programming in Spring 5](https://www.packtpub.com/application-development/hands-reactive-programming-spring-5); it really helped level-up my understanding and practice on more than a few occasions
* [Stephane Maldini](https://github.com/smaldini) for all the coaching on [Reactor](https://projectreactor.io); especially error handling
* [Mark Paluch](https://github.com/mp911de) for coaching on [R2DBC](https://r2dbc.io) and helping me untangle dependencies
* [Peter Royal](https://github.com/osi) for [assistance](https://gitter.im/reactor/reactor?at=5c38c24966f3433023afceb2) troubleshooting some design and implementation of policy execution tasks

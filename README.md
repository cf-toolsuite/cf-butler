# Pivotal Application Service > Butler

[![Beta](https://img.shields.io/badge/Stability-Beta-orange)](https://img.shields.io/badge/Stability-Beta-orange) [![Build Status](https://travis-ci.org/pacphi/cf-butler.svg?branch=master)](https://travis-ci.org/pacphi/cf-butler) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-butler/badge.svg?style=plastic)](https://snyk.io/test/github/pacphi/cf-butler) [![Release](https://jitpack.io/v/pacphi/cf-butler.svg)](https://jitpack.io/#pacphi/cf-butler/master-SNAPSHOT) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


You are a platform operator working for a Fortune 500 enterprise.  You've witnessed first-hand how the product development teams your team supports are super productive; happily invoking `cf push`, `cf create-service` and `cf bind-service` many times per day to deploy applications, create services and bind them to those applications.

This is great, except that over time, on your non-production foundations, as you've browsed organizations and spaces, you have noticed a large number of stopped application instances and orphaned services (i.e., those not bound to any applications).

Reaching out to each development team to tell them to clean-up has become a chore.  Why not implement some automation that allows you a) to obtain snapshot and usage reports and b) define and enforce some house-keeping policies for your non-production foundations where applications and services are perhaps more volatile and c) easily handle multi-organization or system-wide use-cases like application instance scaling or stack changes?

This is where `cf-butler` has your back.

# Table of Contents

  * [What does it do?](#what-does-it-do)
      * [Tell me, don't show me](#tell-me-dont-show-me)
      * [And what about Pivotal Telemetry Collector?](#and-what-about-pivotal-telemetry-collector)
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
        * [Query policies](#query-policies)
      * [To set the operations schedule](#to-set-the-operations-schedule)
      * [To discriminate user from service accounts](#to-discriminate-user-from-service-accounts)
      * [Filtering organizations](#filtering-organizations)
        * [Blacklist](#blacklist)
        * [Whitelist](#whitelist)
      * [Integration w/ Operations Manager](#integration-w-operations-manager)
  * [How to Build](#how-to-build)
      * [Alternative build with MySQL support](#alternative-build-with-mysql-support)
  * [How to Run with Gradle](#how-to-run-with-gradle)
  * [How to Run with Docker](#how-to-run-with-docker)
  * [How to deploy to Pivotal Application Service](#how-to-deploy-to-pivotal-application-service)
      * [with Username and password authorization](#with-username-and-password-authorization)
      * [with SSO authorization](#with-sso-authorization)
      * [using scripts](#using-scripts)
  * [Endpoints](#endpoints)
      * [Operations Manager](#operations-manager)
      * [Pivotal Network](#pivotal-network)
      * [Events](#events)
      * [Metadata](#metadata)
      * [Snapshot](#snapshot)
      * [Accounting](#accounting)
      * [Policies](#policies)
  * [Credits](#credits)

## What does it do?

Please take 5-10 mintues to view this short video demonstration to get a sense of what `cf-butler` can do.

[![Youtube screenshot](cf-butler-demo.jpg)](https://youtu.be/IyLJfC6N60Q)

### Tell me, don't show me

Cf-butler exposes a number of self-service endpoints that perform house-keeping for your foundation.  You define policies and an execution schedule.  E.g., applications and service instances could be removed based on policy crtieria.  Cf-butler also provides detail and summary snapshot reporting on all applications, service instances, user accounts, organizations and spaces.  Lastly, cf-butler [aspires](https://github.com/pacphi/cf-butler/issues/62) to provide operators insight into the "freshness" of installed tiles, stemcells and buildpacks.

### And what about Pivotal Telemetry Collector?

[Pivotal Telemetry Collector](https://docs.pivotal.io/telemetry/1-0/index.html) supports collection of configuration data from Operations Manager, certificate data from Credhub, and usage data from Pivotal Application Service.  Customers download and install a [CLI](https://network.pivotal.io/products/pivotal-telemetry-collector/) from Pivotal Network.  Typically, a [Concourse](https://concourse-ci.org) [pipeline](https://docs.pivotal.io/telemetry/1-0/using-concourse.html) is configured to automate collection.  The result of collection is a foundation details [tarball](https://docs.pivotal.io/telemetry/1-0/data.html#collected). Customers may opt to transmit this data to Pivotal.

Telemetry is also available for [PCF Dev](https://docs.pivotal.io/pcf-dev/telemetry.html) and [Pivotal Container Service](https://docs.pivotal.io/runtimes/pks/1-4/telemetry.html).

> Note: the [Pivotal Telemetry]((https://pivotal.io/legal/telemetry)) program is opt-in.

Cf-butler is configured and deployed as an application instance. Its capabilities overlap only on usage data collection from Pivotal Application Service.  However, cf-butler performs other useful duties like a) snapshot usage reporting and b) policy registration and execution.


## Prerequisites

Required

* [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) admin account

Optional

* [Pivotal Network](https://network.pivotal.io) account
* [Pivotal Operations Manager](https://pivotal.io/platform/pcf-components/pcf-ops-manager) admin account


## Tools

* [git](https://git-scm.com/downloads) 2.20.1 or better
* [JDK](http://openjdk.java.net/install/) 11 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.41.0 or better
* [uaac](https://github.com/cloudfoundry/cf-uaac) 4.1.0 or better


## Clone

```
git clone https://github.com/pacphi/cf-butler.git
```


## How to configure

Make a copy of then edit the contents of the `application.yml` file located in `src/main/resources`.  A best practice is to append a suffix representating the target deployment environment (e.g., `application-pws.yml`, `application-pcfone.yml`). You will need to provide administrator credentials to Apps Manager for the foundation if you want the butler to keep your entire foundation tidy.

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

We'll use this file later as input configuration for the creation of either a [credhub](https://docs.pivotal.io/credhub-service-broker/using.html) or [user-provided](https://docs.cloudfoundry.org/devguide/services/user-provided.html#credentials) service instance.

> Replace occurrences of `xxxxx` above with appropriate values

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a Pivotal Application Service API endpoint
* `token.provider` - Pivotal Application Service authorization token provider, options are: `userpass` or `sso`
* `pivnet.apiToken` - a Pivotal Network legacy API Token, visit your [profile](https://network.pivotal.io/users/dashboard/edit-profile)

Based on choice of the authorization token provider

#### Username and password

* `cf.username` - a Pivotal Application Service account username (typically an administrator account)
* `cf.password` - a Pivotal Application Service account password

#### Single-sign on

* `cf.refreshToken` - the refresh token to be found within `~/.cf/config.json` after your authenticate

### General configuration notes

If you copied and appended a suffix to the original `application.yml` then you would set `spring.profiles.active` to be that suffix

E.g., if you had a configuration file named `application-pws.yml`

```
./gradlew bootRun -Dspring.profiles.active=pws
```

> See the [samples](samples) directory for some examples of configuration when deploying to [Pivotal Web Services](https://login.run.pivotal.io/login) or [PCF One](https://login.run.pcfone.io/login).

### Using an external database

By default `cf-butler` employs an in-memory [H2](http://www.h2database.com) instance.

If you wish to configure an external database you must set set `spring.r2dbc.*` properties as described [here](https://github.com/spring-projects-experimental/spring-boot-r2dbc).

Before you `cf push`, stash the credentials for your database in `config/secrets.json` like so

```
{
  "R2DBC_URL": "rdbc:postgresql://<server>:<port>/<database>",
  "R2DBC_USERNAME": "<username>",
  "R2DBC_PASSWORD": "<password>"
}
```

> Replace place-holders encapsulated in `<>` above with real credentials

Or you may wish to `cf bind-service` to a database service instance. In this case you must abide by a naming convention. The name of your service instance must be `cf-butler-backend`.

[DDL](https://en.wikipedia.org/wiki/Data_definition_language) scripts for each supported database are managed underneath [src/main/resources/db](src/main/resources/db). Supported databases are: [h2](src/main/resources/db/h2/schema.ddl), [mysql](src/main/resources/db/mysql/schema.ddl) and [postgresql](src/main/resources/db/postgresql/schema.ddl).

> A sample [script](scripts/deploy.postgres.sh) and [secrets](samples/secrets.pws.with-postgres.json) for deploying `cf-butler` to Pivotal Web Services with an [ElephantSQL](https://www.elephantsql.com) backend exists for your perusal.  If you're rather interested in MySQL as a backend, take a look at this version of [secrets](samples/secrets.pws.with-mysql.json) and the accompanying [script](scripts/deploy.mysql.sh).

### Managing policies

Creation and deletion of policies are managed via API endpoints by default. When an audit trail is important to you, you may opt to set `cf.policies.provider` to `git`.  When you do this, you shift the lifecycle management of policies to Git.  You will have to specify additional configuration, like

* `cf.policies.uri` the location of the repository that contains policy files in JSON format
* `cf.policies.commit` the commit id to pull from
  * if this property is missing the latest commit will be used
* `cf.policies.filePaths` an array of file paths of policy files

Policy files must adhere to a naming convention where:

* a filename ending with `-AP.json` encapsulates an individual [ApplicationPolicy](src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java)
* a filename ending with `-SIP.json` encapsulates an individual [ServiceInstancePolicy](src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java)

A sample Github repository exists [here](https://github.com/pacphi/cf-butler-sample-config).

Have a look at [secrets.pws.json](samples/secrets.pws.json) for an example of how to configure secrets for deployment of `cf-butler` to PAS integrating with the aforementioned sample Github repository.

On startup `cf-butler` will read files from the repo and cache in a database.  Each policy's id will be set to the commit id.

#### Hygiene Policies

Hygiene policies are useful when you want to search for and report on dormant workloads, notifying both the operator and for each workload the author and/or his/her space compadres.  Workloads are running applications and service instances that have not been updated in N or more days from the date/time of the policy execution.

> Note: hygiene policy configuration has a special case where if the `days-since-last-update` property value is set to `-1` then ALL workloads (minus the blacklist) are included in the respective notifications.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-HP.json` encapsulates an individual [HygienePolicy](src/main/java/io/pivotal/cfapp/domain/HygienePolicy.java)

See additional property requirements in Query policies and the aforementioned sample Github repository.


#### Legacy Policies

Legacy policies are useful when you want to search for and report on applications deployed to a legacy stack (e.g., windows2012R2, cflinuxfs2), notifying both the operator and for each application the author and/or his/her space compadres.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-LP.json` encapsulates an individual [LegacyPolicy](src/main/java/io/pivotal/cfapp/domain/LegacyPolicy.java)

See additional property requirements in Query policies and the aforementioned sample Github repository.


#### Query policies

Query policies are useful when you want to step out side the canned snapshot reporting capabilties and leverage the underlying [schema](https://github.com/pacphi/cf-butler/tree/master/src/main/resources/db) to author one or more of your own queries and have the results delivered as comma-separated value attachments using a defined email notification [template](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/EmailNotificationTemplate.java).

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

Furthermore, you will need to define additional properties depending on which engine you chose.  Checkout the secrets profile in [application.yml](https://github.com/pacphi/cf-butler/blob/master/src/main/resources/application.yml) to get to know what they are.

E.g, if you intended to use [sendgrid](https://www.sendgrid.com) as your email notification engine then your secrets-{env}.yml might contain

```
  "NOTIFICATION_ENGINE": "sendgrid",
  "SENDGRID_API-KEY": "replace_me"
```

### To set the operations schedule

Update the value of the `cron` properties in `application.yml`.  Consult this [article](https://riptutorial.com/spring/example/21209/cron-expression) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.

> `cron` has two sub-properties: `collection` and `execution`.  Make sure `execution` is scheduled to trigger after `collection`.

### To discriminate user from service accounts

Consult [PasSettings.java](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/config/PasSettings.java#L25) for the default pattern value used to discriminate between user and service accounts.  You may override the default by adding to

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

Within each [ApplicationPolicy](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java), [ServiceInstancePolicy](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java), or [HygienePolicy](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/HygienePolicy.java) you may optionally specify a list of organizations that will be whitelisted.  Policy execution will be restricted to just these organizations in the whitelist.

> If the organization whitelist is not specified in a policy then that policy's execution applies to all organizations on the foundation (except for those in the organization blacklist).

### Integration w/ Operations Manager

You must add the following configuration properties to `application-{env}.yml` if you want to enable integration with an operations manager instance

* `om.apiHost` - a Pivotal Operations Manager API endpoint
* `om.enabled` - a boolean property that must be set to `true`

> the `{env}` filename suffix above denotes the Spring Profile you would activate for your environment

or

Add entries in your `config/secrets.json` like

```
  "OM_API-HOST": "xxxxxx",
  "OM_ENABLED": true
```

## How to Build

```
./gradlew build
```

### Alternative build with MySQL support

If you want to target a MySQL database as your back-end you will need to use an alternate Gradle build file.  It adds a dependency on [r2dbc-mysql](https://github.com/mirromutth/r2dbc-mysql) which is sourced from a [jitpack.io](https://jitpack.io/#mirromutth/r2dbc-mysql/master-SNAPSHOT) repository.

```
./gradlew -b build.w-mysql.gradle
```

## How to Run with Gradle

```
./gradlew bootRun -Dspring.profiles.active={target_foundation_profile}
```
where `{target_foundation_profile}` is something like `pws` or `pcfone`

> You'll need to manually stop to the application with `Ctrl+C`

## How to Run with Docker

You might choose this option when experimenting with an external database provider image like [postgres](https://github.com/docker-library/postgres/blob/6c3b27f1433ad81675afb386a182098dc867e3e8/11/alpine/Dockerfile) or [mysql](https://github.com/docker-library/mysql/blob/26380f33a0fcd07dda35e37516eb24eaf962845c/5.7/Dockerfile)

Build

```
docker build -t pivotalio/cf-butler:latest .
```

Run

Start database

```
docker run --name butler-mysql -e MYSQL_DATABASE=butler -e MYSQL_ROOT_PASSWORD=p@ssw0rd! -e MYSQL_USER=butler -e MYSQL_PASSWORD=p@ssw0rd -p 3306:3306 -d mysql:5.7.26
```
> MySQL

or

```
docker run --name butler-postgres -e POSTGRES_DB=butler -e POSTGRES_USER=butler -e POSTGRES_PASSWORD=p@ssw0rd -p 5432:5432 -d postgres:11.4
```
> PostgreSQL


Start application

```
docker run -it --rm -e SPRING_PROFILES_ACTIVE={env} pivotalio/cf-butler
```
> **Note**: You should have authored an `application-{env}.yml` that encapsulates the appropriate configuration within `src/main/resources` before you built the `cf-butler-{version}-SNAPSHOT.jar` artifact with Gradle

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


## How to deploy to Pivotal Application Service

### with Username and password authorization

The following instructions explain how to get started when `token.provider` is set to `userpass`

Authenticate to a foundation using the API endpoint.

> E.g., login to [Pivotal Web Services](https://login.run.pivotal.io)

```
cf login -a https://api.run.pivotal.io
```

### with SSO authorization

The following instructions explain how to get started when `token.provider` is set to `sso`

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
  "TOKEN_PROVIDER": "sso",
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

> Note: If you are seeing [OutOfMemory exceptions](https://dzone.com/articles/troubleshooting-problems-with-native-off-heap-memo) shortly after startup you may need to [cf scale](https://docs.run.pivotal.io/devguide/deploy-apps/cf-scale.html#vertical) the available memory for large foundations.

## Endpoints

These REST endpoints have been exposed for reporting and administrative purposes.

### Operations Manager

These endpoints are only available when the `om.enabled` property is set to `true`. `om.apiHost`, `om.username` and `om.password` properties must also have been defined.  Mimics a reduced set of the [Operations Manager API](https://docs.pivotal.io/pivotalcf/2-6/opsman-api/).

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
> Only available from Operations Manager 2.6.0 onward.

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
            "currently-installed-version": "2.6.0",
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


### Pivotal Network

These endpoints are only available when the `pivnet.enabled` property is set to `true`. A valid `pivnet.apiToken` property must also have been defined.  Mimics a reduced set of the [Pivotal Network API](https://network.pivotal.io/docs/api).

```
GET /store/product/catalog
```
> Retrieves a list of all products from Pivotal Network (includes buildpacks, stemcells and tiles)

```
GET /store/product/releases?q=latest
```
> Returns a list of the latest available releases for all products on Pivotal Network (includes buildpacks, stemcells and tiles)

```
GET /store/product/releases?q=all
```
> Returns a list of all available releases for all products on Pivotal Network (includes buildpacks, stemcells and tiles)


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
> Return resource with id, created date, last updated date, and associated metadata; where `{type}` is the id field of a [ResourceType](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ResourceType.java) enum and `{id}` is the guid of a resource.

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
      "wlund@pivotal.io",
      "akarode@pivotal.io"
    ],
    managers: [
      "wlund@pivotal.io",
      "akarode@pivotal.io"
    ],
    users: [
      "wlund@pivotal.io",
      "akarode@pivotal.io"
    ],
    user-count: 2,
  },
  {
    organization: "Northwest",
    space: "arao",
    auditors: [ ],
    developers: [
      "arao@pivotal.io"
    ],
    managers: [
      "arao@pivotal.io"
    ],
    users: [
      "arao@pivotal.io"
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

```
GET /snapshot/detail/ai
```
> Provides lists of all applications in comma-separated value format

Sample output

```
Application inventory detail from api.sys.demo.ironleg.me generated 2019-08-19T05:39:46.659078.


organization,space,application id,application name,buildpack,buildpack version,image,stack,running instances,total instances,memory_used,disk_used,urls,last pushed,last event,last event actor,last event time,requested state
"blast-radius","substratum","440c21b9-50c3-4135-8a1c-6e1623d27ba9","cf-butler","java","v4.18",,"cflinuxfs3","1","1","0.412717924","0.195510272","cf-butler.apps.demo.ironleg.me","2019-08-01T09:47:33","audit.app.droplet.create","admin","2019-08-01T09:58:55","started"
"blast-radius","substratum","f34fdbcb-3cb8-4d76-90ad-aafb1e64b5b6","cf-hoover","java","v4.18",,"cflinuxfs3","1","1","0.241331355","0.187875328","cf-hoover.apps.demo.ironleg.me","2019-07-30T06:06:28","audit.app.droplet.create","admin","2019-07-30T06:18:42","started"
"blast-radius","substratum","fdcc6a1c-d457-4418-b80c-9b630c59df92","cf-hoover-ui","java","v4.18",,"cflinuxfs3","1","1","0.376542052","0.213118976","cf-hoover-ui.apps.demo.ironleg.me","2019-07-30T06:22:27","audit.app.droplet.create","admin","2019-07-30T06:23:47","started"
"credhub-service-broker-org","credhub-service-broker-space","8b71f3f7-e678-49a1-b33f-9602a361fd6f","credhub-broker-1.3.3","binary","v1.0.32",,"cflinuxfs3","1","1","0.015036871","0.009777152","credhub-broker.apps.demo.ironleg.me","2019-07-28T17:37:13","audit.app.droplet.create","system_services","2019-07-28T17:37:43","started"
"killens","dev","b1406fd6-7394-4182-928a-e9e7c606f711","lighthouseweb","hwc","v3.1.10",,"windows","1","1","0.323162112","0.131223552","lighthouseweb.apps.demo.ironleg.me","2019-08-13T12:14:16","audit.app.droplet.create","mkillens@pivotal.io","2019-08-13T12:14:48","started"
"p-spring-cloud-services","5095d5ca-a7cf-4456-b8ea-2cfd5549327c","a5508727-788c-446a-aa17-1f8c028196e8","config-server","java","v4.18",,"cflinuxfs3","1","1","0.246387556","0.169598976","config-server-8095d5ca-a7cf-4456-b8ea-2cfd5549327c.apps.demo.ironleg.me","2019-07-30T06:08:25","audit.app.droplet.create",,"2019-07-30T06:09:23","started"
"p-spring-cloud-services","instances","c2917b8d-54e1-48c7-a3d4-38137d4623cb","eureka-29239c3f-b89e-4da1-b579-0338176146f8","java","v4.18",,"cflinuxfs3","1","1","0.372844233","0.198602752","eureka-24239c3f-b89e-4da1-b579-0338176146f8.apps.demo.ironleg.me","2019-07-30T06:08:46","audit.app.droplet.create",,"2019-07-30T06:11:14","started"
```

```
GET /snapshot/detail/si
```
> Provides a list of all service instances in comma-separated value format

Sample output

```
Service inventory detail from api.sys.cf.zoo.labs.foo.org generated 2019-03-22T07:07:28.166022.


organization,space,service instance id,name,service,description,plan,type,bound applications,last operation,last updated,dashboard url,requested state
"mvptime","default",,"reactive-cassy-secrets","credhub","Stores configuration parameters securely in CredHub","default","managed_service_instance","reactive-cassy","create","2018-11-20T00:00",,"succeeded"
"planespotter","default",,"planespotter-vault","credhub","Stores configuration parameters securely in CredHub","default","managed_service_instance","planespotter-alpha","update","2019-03-21T00:00",,"succeeded"
```

```
GET /snapshot/detail/dormant/{daysSinceLastUpdate}
```
> Provides a list of dormant workloads. A workload is either an application or service instance.  An application is considered dormant when the last occasion of among `audit.app.create`, `audit.app.update` or `audit.app.restage` events transpired `daysSinceLastUpdate` or longer from the time of request.  A service instance is considered dormant when the last occasion of among `audit.service_instance.create`, `audit.service_instance.update`, `audit.user_provided_service_instance.create` or `audit.user_provided_service_instance.update` events transpired `daysSinceLastUpdate` or longer from the time of request.

```
GET /snapshot/detail/legacy/{stacks}
```
> Returns a list of all applications that have been deployed on legacy stacks like `windows2012R2`, `cflinusfs2`.

```
GET /snapshot/detail/users
```
> Provides a list of all space users (ignoring role) in comma-separated value format by organization and space, where multiple users in each space are comma-separated. Service accounts are filtered.

Sample output

```
User accounts from api.sys.cf.zoo.labs.foo.org generated 2019-05-17T00:19:45.932764.


organization,space,user accounts
"mvptime","default","cphillipson@pivotal.io,bruce.lee@kungfulegends.com,vmanoharan@pivotal.io"
"planespotter","default","stan.lee@marvel.com,vmanoharan@pivotal.io"
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


### Accounting

> Note: `/accounting/**` endpoints below require a user with `cloud_controller.admin` or `usage_service.audit` scope.  See [Creating and Managing Users with the UAA CLI (UAAC)](https://docs.pivotal.io/pivotalcf/2-5/uaa/uaa-user-management.html).

```
GET /accounting/applications
```
> Produces a system-wide account report of [application usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#app-usage)

> **Note**: Report excludes application instances in the `system` org

```
GET /accounting/services
```
> Produces a system-wide account report of [service usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#service-usage)

> **Note**: Report excludes user-provided service instances

```
GET /accounting/tasks
```
> Produces a system-wide account report of [task usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#task-usage)

```
GET /accounting/applications/{orgName}/{startDate}/{endDate}
```
> Produces an [application usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#org-app-usage) constrained to single organization and time period

```
GET /accounting/services/{orgName}/{startDate}/{endDate}
```
> Produces a [service usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#org-service-usage) constrained to single organization and time period

```
GET /accounting/tasks/{orgName}/{startDate}/{endDate}
```
> Produces a [task usage](https://docs.pivotal.io/pivotalcf/2-6/opsguide/accounting-report.html#org-task-usage) constrained to single organization and time period


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
      "operator-email-template": {
        "from": "admin@nowhere.me",
        "to": [
          "cphillipson@pivotal.io"
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
        "body": "<h3>TThese applications are deployed to a legacy stack</h3><p>To avoid repeated notification:</p><ul><li>for each application please execute a cf push and update the stack to a modern alternative or cf delete</li></ul><p>depending on whether or not you want to keep the workload running.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report"
      },
      "operator-email-template": {
        "body": "<h3>These applications are deployed to a legacy stack</h3><p>Results are herewith attached for your consideration.</p>",
        "from": "admin@pcf.demo.ironleg.me",
        "subject": "Legacy Policy Sample Report",
        "to": [
          "cphillipson@pivotal.io"
        ]
      },
      "organization-whitelist": [],
      "stacks": [
        "cflinuxfs2",
        "windows2012R2"
      ]
    }
  ]
}
```
> Establish policies to delete and scale applications, delete service instances, and query for anything from schema. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

> Consult the [java.time.Duration](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-) javadoc for other examples of what you can specify when setting values for `from-duration` properties above.

```
GET /policies
```
> List current policies

```
DELETE /policies
```
> Delete all established policies. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

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
DELETE /policies/application/{id}
```
> Delete an application policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

```
DELETE /policies/serviceInstance/{id}
```
> Delete a service instance policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

```
DELETE /policies/query/{id}
```
> Delete a query policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

```
DELETE /policies/hygiene/{id}
```
> Delete a hygiene policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

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


## Credits

* [Oleh Dokuka](https://github.com/OlegDokuka) for writing [Hands-on Reactive Programming in Spring 5](https://www.packtpub.com/application-development/hands-reactive-programming-spring-5); it really helped level-up my understanding and practice on more than a few occasions
* [Stephane Maldini](https://github.com/smaldini) for all the coaching on [Reactor](https://projectreactor.io); especially error handling
* [Mark Paluch](https://github.com/mp911de) for coaching on [R2DBC](https://r2dbc.io) and helping me untangle Gradle dependencies
* [Peter Royal](https://github.com/osi) for [assistance](https://gitter.im/reactor/reactor?at=5c38c24966f3433023afceb2) troubleshooting some design and implementation of policy execution tasks

# Pivotal Application Service > Butler
 
[![Build Status](https://travis-ci.org/pacphi/cf-butler.svg?branch=master)](https://travis-ci.org/pacphi/cf-butler) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-butler/badge.svg)](https://snyk.io/test/github/pacphi/cf-butler)

> Status: Incubating

You are a platform operator working for a Fortune 500 enterprise.  You've witnessed first-hand how the product development teams your team supports are super productive; happily invoking `cf push`, `cf cs` and `cf bs` many times per day to deploy applications, create services and bind them to those applications.  

This is great, except that over time, on your non-production foundations, you have noticed in your [cf-app-inventory-report](https://github.com/pacphi/cf-app-inventory-report) and [cf-service-inventory-report](https://github.com/pacphi/cf-service-inventory-report) results a large number of stopped application instances and orphaned services (i.e., those not bound to any applications).

Reaching out to each development team to tell them to clean-up has become a chore.  Why not implement some automation that allows you to define and enforce some house-keeping policies for your non-production foundations where applications and services are perhaps more volatile?

This is where `cf-butler` has your back.

## What does it do?

Please take 5-10 mintues to view this short video demonstration to get a sense of what `cf-butler` can do.

[![Youtube screenshot](cf-butler-demo.jpg)](https://youtu.be/IyLJfC6N60Q)
 

## Prerequisites

Required

* [Pivotal Application Service](https://pivotal.io/platform/pivotal-application-service) account


## Tools

* [git](https://git-scm.com/downloads) 2.20.1 or better
* [JDK](http://openjdk.java.net/install/) 11 or better
* [cf](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) CLI 6.41.0 or better


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
	"CF_API-HOST": "xxxxx",
	"CF_USERNAME": "xxxxx",
	"CF_PASSWORD": "xxxxx"
}
```

We'll use this file later as input configuration for the creation of either a [credhub](https://docs.pivotal.io/credhub-service-broker/using.html) or [user-provided](https://docs.cloudfoundry.org/devguide/services/user-provided.html#credentials) service instance.

> Replace occurrences of `xxxxx` above with appropriate values

### Minimum required keys

At a minimum you should supply values for the following keys

* `cf.apiHost` - a Pivotal Application Service API endpoint
* `token.provider` - Authorization token provider, options are: `userpass` or `sso`

Based on choice the authorization token provider

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

### Managing policies

Creation and deletion of policies are managed via API endpoints by default. When an audit trail is important to you, you may opt to set `cf.policies.provider` to `git`.  When you do this, you shift the lifecycle management of policies to Git.  You will have to specify additionaa configuration, like

* `cf.policies.uri` the location of the repository that contains policy files in JSON format
* `cf.policies.commit` the commit id to pull from
* `cf.policies.filePaths` an array of file paths of policy files

Policy files must adhere to a naming convention where:

* a filename ending with `-AP.json` encapsulates an individual [ApplicationPolicy](src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java)
* a filename ending with `-SIP.json` encapsulates an individual [ServiceInstancePolicy](src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java)

A sample Github repository exists [here](https://github.com/pacphi/cf-butler-config-sample).

Have a look at [secrets.pws.json](samples/secrets.pws.json) for an enable of how to configure secrets for deployment of `cf-butler` to PAS integrating with the aforementioned sample Github repository.

### To set the operations schedule

Update the value of the `cron` properties in `application.yml`.  Consult this [article](https://www.baeldung.com/spring-scheduled-tasks) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.  

> `cron` has two sub-properties: `collection` and `execution`.  Make sure `execution` is scheduled to trigger after `collection`.

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

Within each [ApplicationPolicy](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java) or [ServiceInstancePolicy](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java) you may optionally specify a list of organizations that will be whitelisted.  Policy execution will be restricted to just these organizations in the whitelist.

> If the organization whitelist is not specified in a policy then that policy's execution applies to all organizations on the foundation (except for those in the organization blacklist).

### Troubleshooting

To have access to a database management [console](http://hsqldb.org/doc/guide/running-chapt.html#rgc_access_tools) which would allow you to execute queries against the in-memory database, you will need to set an additional JVM argument.  

```
-Djava.awt.headless=false
```

> Note: this is not an available option when deploying to a PAS foundation.


## How to Build

```
./gradlew build
```


## How to Run

```
./gradlew bootRun -Dspring.profiles.active={target_foundation_profile}
```
where `{target_foundation_profile}` is something like `pws` or `pcfone`

> You'll need to manually stop to the application with `Ctrl+C`


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

Deploy the app (w/ a user-provided service instance vending secrets)

```
./deploy.sh
```

Deploy the app (w/ a Credhub service instance vending secrets)

```
./deploy.sh --with-credhub
```

Shutdown and destroy the app and service instances

```
./destroy.sh
```


## Endpoints

These REST endpoints have been exposed for administrative purposes.  

```
GET /report
```
> Produces `text/plain` historical output detailing what applications and service instances have been removed

```
POST /policies

{
  "application-policies": [
    {
      "description": "Remove stopped applications retroactively as of an explicit date",
      "state": "stopped",
      "from-datetime": "2018-12-01T08:00:00",
      "delete-services": "false"
    },
    {
      "description": "Remove stopped applications that are older than some duration from now and restricted to whitelisted organizations",
      "state": "stopped",
      "from-duration": "P1D",
      "delete-services": "true",
      "organization-whitelist": [ "zoo-labs" ]
    }
  ],
  "service-instance-policies": [
    {
      "description": "Remove orphaned services retroactively as of an explicit date",
      "from-datetime": "2018-12-01T08:00:00"
    },
    {
      "description": "Remove orphaned services that are older than some duration from now and restricted to whitelisted organizations",
      "from-duration": "P1D",
      "organization-whitelist": [ "zoo-labs" ]
    }
  ]
}
```

> Establish policies to remove stopped applications and/or orphaned services. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

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
DELETE /policies/application/{id}
```

> Delete an application policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.

```
DELETE /policies/serviceInstance/{id}
```

> Delete a service instance policy by its id. This endpoint is only available when `cf.policies.provider` is set to `dbms`.


## Credits

* [Peter Royal](https://github.com/osi) for [assistance](https://gitter.im/reactor/reactor?at=5c38c24966f3433023afceb2) troubleshooting some method implementation in [ApplicationPolicyExecutorTask](https://github.com/pacphi/cf-butler/blob/master/src/main/java/io/pivotal/cfapp/task/AppPolicyExecutorTask.java)

# Pivotal Application Service > Butler

> This project is in alpha state. Work is on-going.
 
[![Build Status](https://travis-ci.org/pacphi/cf-butler.svg?branch=app-deploy)](https://travis-ci.org/pacphi/cf-butler) [![Known Vulnerabilities](https://snyk.io/test/github/pacphi/cf-butler/badge.svg)](https://snyk.io/test/github/pacphi/cf-butler)

// TODO add this application's reason for existence here

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

* `cf.passcode` - a temporary one-time passcode

> Note: SSO is not yet implemented

### General configuration notes

If you copied and appended a suffix to the original `application.yml` then you would set `spring.profiles.active` to be that suffix 

E.g., if you had a configuration named `application-pws.yml`

```
./gradlew bootRun -Dspring.profiles.active=pws
```

> See the [samples](samples) directory for some examples of configuration when deploying to [Pivotal Web Services](https://login.run.pivotal.io/login) or [PCF One](https://login.run.pcfone.io/login).

### To set the operations schedule

Update the value of the `cron` properties in `application.yml`.  Consult this [article](https://www.baeldung.com/spring-scheduled-tasks) and the [Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron--) to understand how to tune it for your purposes.


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
> E.g., login to [Pivotal Web Services](https://login.run.pcfone.io)

```
cf login -a https://api.run.pcfone.io -sso
```

Visit the link in the password prompt to retrieve a temporary passcode (e.g., https://login.run.pcfone.io/passcode)

> Complete the login process

Visit https://login.run.pcfone.io/passcode again

Make a note of the passcode because you will need to use it in your `config/secrets.json` which at a minimum should contain

```
{
  "TOKEN_PROVIDER": "sso",
  "CF_API-HOST": "xxxxx",
  "CF_PASSCODE": "xxxxx",
}
```

> Note: this procedure is not yet functioning as desired because SSO capability is not fully implemented

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

## What does this task do?

// TODO Illustrate what this app does.  Perhaps record a demonstration video and link here?

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
      "unbind-services": "false",
      "delete-services": "false"
    },
    {
      "description": "Remove stopped applications that are older than some duration from now",
      "state": "stopped",
      "from-duration": "P1D",
      "unbind-services": "true",
      "delete-services": "true"
    }
  ],
  "service-instance-policies": [
    {
      "description": "Remove orphaned services retroactively as of an explicit date",
      "from-datetime": "2018-12-01T08:00:00"
    },
    {
      "description": "Remove orphaned services that are older than some duration from now",
      "from-duration": "P1D"
    }
  ]
}
```

> Establish policies to remove stopped applications and/or orphaned services. 

> Consult the [java.time.Duration](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-) javadoc for other examples of what you can specify when setting values for `from-duration` properties above.

```
GET /policies
```

> List current policies

```
DELETE /policies
```

> Delete all established policies

## Credits

// TODO
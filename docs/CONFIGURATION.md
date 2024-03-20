# VMware Tanzu Application Service > Butler

## How to configure

Make a copy of then edit the contents of the `application.yml` file located in `src/main/resources`.  A best practice is to append a suffix representing the target deployment environment (e.g., `application-pcfone.yml`). You will need to provide administrator credentials to Apps Manager for the foundation if you want the butler to keep your entire foundation tidy.

> You really should not bundle configuration with the application. To take some of the sting away, you might consider externalizing and/or [encrypting](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_encryption_and_decryption) this configuration.

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

> See the [samples](../samples) directory for a few examples of configuration options when deploying to a foundation.

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

> Review the sample scripts for deploying [postgres](../scripts/deploy.postgres.sh) and [mysql](../scripts/deploy.mysql.sh).  And consult the corresponding secrets samples for [postgres](../samples/secrets.pws.with-postgres.json) and [mysql](../samples/secrets.pws.with-mysql.json) when you intend to transact an externally hosted database.


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

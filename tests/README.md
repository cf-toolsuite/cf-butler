# Tests for cf-butler

In order to test definition and execution of application and service instance policies we've set up some scripts to orchestrate [cf-cli](http://cli.cloudfoundry.org/en-US/cf/) calls to [push](https://cli.cloudfoundry.org/en-US/cf/push.html), [cs](https://cli.cloudfoundry.org/en-US/cf/create-service.html), [bs](https://cli.cloudfoundry.org/en-US/cf/bind-service.html), [stop](https://cli.cloudfoundry.org/en-US/cf/stop.html).  We've also implemented some test endpoints to trigger on-demand collection and policy execution.

## How to Run Tests

### Start the app

You should activate the `on-demand` profile in addition to a profile that allows you to appropriately connect to a target foundation

```
./gradlew bootRun -Dspring.profiles.active=on-demand,{target_foundation_profile}
```
where `{target_foundation_profile}` is something like `pws` or `pcfone`

### Choose a use case

Visit a test sub-folder

> E.g., [stopped-apps-with-no-service-bindings](stopped-apps-with-no-service-bindings)

### Setup

Run scripts to create a test space, setup the test case resources and temporary directory

```
./create-space.sh
./setup.sh
```

### Trigger collection

```
POST /collect
```

### Create a policy

```
POST /policies
```

> See sample policies in each test sub-folder

### Trigger execution

```
POST /policies/execute
```

### Verify execution

Use the [cf-cli](http://cli.cloudfoundry.org/en-US/cf/) to [target](http://cli.cloudfoundry.org/en-US/cf/target.html) an organization and space.  Then verify that applications and/or service instances were removed by inspecting the result of `cf` [apps](http://cli.cloudfoundry.org/en-US/cf/apps.html) and/or [services](http://cli.cloudfoundry.org/en-US/cf/services.html).

### Review the historical report

```
GET /policies/report
```

It should contain records for each application and/or service instance removed.

### Teardown

Run scripts to delete a test space and cleanup the temporary directory

```
./delete-space.sh
./teardown.sh
```

## Endpoints

```
POST /policies/execute
```

> Trigger policy execution

```
POST /collect
```

> Trigger collection of application detail, service instance detail, and relationships

## Scripts

`cf-butler` supports a number of policy use cases, like:

* Removing stopped applications retroactively as of an explicit date
	* do not delete bound service instances
	* delete bound service instances
* Removing stopped applications that are older than some duration from now
	* do not delete bound service instances
	* delete bound service instances
* Removing orphaned services retroactively as of an explicit date
* Removing orphaned services that are older than some duration from now
* Scaling application instances from n to m

Sub-folders here-in contain bash scripts for cloning sample applications, building and pushing those applications, creating service instances and binding them to applications, and stopping applications and service instances when appropriate to setup each use case.

At a minimum a sub-folder should contain the following files

* `create-space.sh`
* `delete-space.sh`
* `setup.sh`
* `teardown.sh`
* one or more policy variant json files

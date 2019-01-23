# Demo script for cf-butler

Building from our tests library we should have the facilities to prepare a demo environment (and have that effort be consistent and repeatable) consisting of a number of organizations and spaces containing a number of applications and service instances.  We can set the state of the applications choosing whether or not to a) leave them in stopped state or b) bound to one or more service instances.  We can also choose whether or not to launch service instances unbound to any application.

## Prerequistes

* an account on [PCF One](https://login.run.pcfone.io)
* a [PostgreSQL](https://www.postgresql.org) database (e.g., a [db.t2.micro](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.DBInstanceClass.html) [RDS](https://aws.amazon.com/rds/postgresql/) instance on [AWS](https://aws.amazon.com/free) free tier)

## How to Run Script

### Setup

Edit [postgres.json](postgres.json).  Update the key-value pairs for the host, port and username/password credentials that will be used to connect to a Postgres database instance by one of the applications in this demo.
 
Edit [secrets.json](secrets.json) and update the value of `CF_PASSCODE` (used by `cf-butler` for single sign-on authentication), then execute these scripts in the following order

```
./create-spaces.sh
./build.sh
./deploy.pcfone.sh
```

> Setting up the demo environment can take up to 30-45m.  Go grab a coffee.

### Verify setup

Login to Apps Manager for PCF One and check to see if 

* organization contains spaces
* each space hosts applications and service instances desired 

### Create a policy

```
POST /policies
```

> Use the [policy.json](policy.json) for the payload

### Trigger execution

```
POST /policies/execute
```

### Verify execution

Use the [cf-cli](http://cli.cloudfoundry.org/en-US/cf/) to [target](http://cli.cloudfoundry.org/en-US/cf/target.html) an organization and space.  Then verify that applications and/or service instances were removed by inspecting the result of `cf` [apps](http://cli.cloudfoundry.org/en-US/cf/apps.html) and/or [services](http://cli.cloudfoundry.org/en-US/cf/services.html).

### Review the historical report

```
GET /report
```

It should contain records for each application and/or service instance removed.

### Teardown

Run scripts to delete a test space and cleanup the temporary directory

```
./delete-spaces.sh
./teardown.sh
```

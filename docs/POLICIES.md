# VMware Tanzu Application Service > Butler

* [Managing Policies](#managing-policies)
  * [Application and Service Instance](#application-and-service-instance-policies)
  * [Hygiene](#hygiene-policies)
  * [Legacy](#legacy-policies)
  * [Resource Notification](#resource-notification-policies)
  * [Endpoint](#endpoint-policies)
  * [Query](#query-policies)


## Managing policies

Creation and deletion of policies are managed via API endpoints by default. When an audit trail is important to you, you may opt to set `cf.policies.git.uri` -- this property specifies the location of the repository that contains policy files in JSON format.

When you do this, you shift the lifecycle management of policies to Git.  You will have to specify additional configuration, like

* `cf.policies.git.commit` the commit id to pull from
  * if this property is missing the latest commit will be used
* `cf.policies.git.filePaths` an array of file paths of policy files

If you want to work with a private repository, then you will have to specify

* `cf.policies.git.username`
* `cf.policies.git.password`

one or both are used to authenticate.  In the case where you may have configured a personal access token, set `cf.policies.git.username` equal to the value of the token.

A sample Github repository exists [here](https://github.com/cf-toolsuite/cf-butler-sample-config).

Have a look at [secrets.pws.json](../samples/secrets.pws.json) for an example of how to configure secrets for deployment of `cf-butler` to TAS integrating with the aforementioned sample Github repository.

On startup `cf-butler` will read files from the repo and cache in a database.  Each policy's id will be set to the commit id.

### Application and Service Instance Policies

Policy files must adhere to a naming convention where:

* a filename ending with `-AP.json` encapsulates an individual [ApplicationPolicy](src/main/java/io/pivotal/cfapp/domain/ApplicationPolicy.java)
* a filename ending with `-SIP.json` encapsulates an individual [ServiceInstancePolicy](src/main/java/io/pivotal/cfapp/domain/ServiceInstancePolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).

### Hygiene Policies

Hygiene policies are useful when you want to search for and report on dormant workloads, notifying both the operator and for each workload the author and/or his/her space compadres.  Workloads are running applications and service instances that have not been updated in N or more days from the date/time of the policy execution.

> Note: hygiene policy configuration has a special case where if the `days-since-last-update` property value is set to `-1` then ALL workloads (minus the blacklist) are included in the respective notifications.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-HP.json` encapsulates an individual [HygienePolicy](src/main/java/io/pivotal/cfapp/domain/HygienePolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).

### Legacy Policies

Legacy policies are useful when you want to search for and report on applications deployed to a legacy stack (e.g., windows2012R2, cflinuxfs2) or service offering (e.g., using a product slug name like p-config-server, p-service-registry, p-mysql), notifying both the operator and for each application the author and/or his/her space compadres.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-LP.json` encapsulates an individual [LegacyPolicy](src/main/java/io/pivotal/cfapp/domain/LegacyPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).


### Resource Notification Policies

Resource Notification policies are useful when you want to generate a report containing resources of a particular type and send that report to email recipients identified by one or more label values ascribed to the those resources.

For example, you might notify primary and/or secondary owners of a collection of a resources.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-RNP.json` encapsulates an individual [ResourceNotificationPolicy](src/main/java/io/pivotal/cfapp/domain/ResourceNotificationPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).

### Endpoint Policies

Endpoint policies are useful when you want to exercise any of the available GET endpoints and have the results sent to one or more designated email recipients.

As mentioned previously the policy file must adhere to a naming convention

* a filename ending with `-EP.json` encapsulates an individual [EndpointPolicy](src/main/java/io/pivotal/cfapp/domain/EndpointPolicy.java)

See additional property requirements in this sample Github [repository](https://github.com/cf-toolsuite/cf-butler-sample-config).


### Query policies

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

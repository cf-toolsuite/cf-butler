# # VMware Tanzu Application Service > Butler

## Troubleshooting

### Startup failures

Sample log output from startup:

```
INFO  o.c.s.FileWatchingX509ExtendedKeyManager - Initialized KeyManager for /etc/cf-instance-credentials/instance.key and /etc/cf-instance-credentials/instance.crt
```

Check to see if you have configured `cf-butler` to work with a Git repository for policies execution.
Verify that `cf-butler` has inbound network access to Git repository.


Potential solutions:

* Allow or restore inbound network access to Git repository from TAS foundation hosting `cf-butler`
* Remove `CF_POLICIES_*` key-value pairs from `secrets.json` used by credhub or user-provided service instance bound to `cf-butler`

If you make updates to configuration consumed by the aforementioned service instance, you will need to:

```
cf unbind-service cf-butler cf-butler-secrets
cf delete-service cf-butler-secrets -f -w

# Create a new Credhub instance that will consume the updated configuration
# Update the path and filename below to your configuration (in JSON format) as necessary
cf create-service cf-butler-secrets cf create-service credhub default cf-butler-secrets -c config/secrets.json

cf bind-service cf-butler cf-butler-secrets
cf restage cf-butler
```

### Database schema not up to date after upgrading to new release

cf-butler was intentionally implemented with [R2DBC](https://r2dbc.io/) which works with and depends upon specific non-blocking database drivers.
When an application instance of cf-butler starts up, it will execute *.ddl found within a src/main/resources/db/{provider} directory. All SQL for creating database objects (like TABLE) is authored with the IF NOT EXISTS conditional.
This has an unfortunate drawback, because sometimes when you upgrade to a newer release, may necessitate update(s) to database object (e.g., an addition, removal, or update to (a) table column(s).
The conditional prevents required updates from executing for persistent providers like MySQL and Postgres, but not for H2.

So, if you have integrated cf-butler with a database instance liek MySQL or Postgres, you will need to manually execute ALTER TABLE commands including the affected columns before attempting to start up a new release.

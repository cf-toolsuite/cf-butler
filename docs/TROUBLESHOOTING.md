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
# VMware Tanzu Application Service > Butler

## Integration w/ Operations Manager

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
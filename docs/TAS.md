# VMware Tanzu Application Service > Butler

## How to deploy to VMware Tanzu Application Service

### with Username and password authorization

The following instructions explain how to get started when `cf.tokenProvider` is set to `userpass`

Authenticate to a foundation using the API endpoint.

> E.g., login to [IBM Cloud](https://cloud.ibm.com/login)

```
cf login -a api.us-south.cf.cloud.ibm.com -u <username> -p <password>
```

### with SSO authorization

The following instructions explain how to get started when `cf.tokenProvider` is set to `sso`

Authenticate to a foundation using the API endpoint

> E.g., login to [Dhaka](https://login.sys.dhaka.cf-app.com/)

```
cf api https://api.sys.dhaka.cf-app.com
cf login --sso
```

Visit the link in the password prompt to retrieve a temporary passcode, then complete the login process

> E.g., `https://login.sys.dhaka.cf-app.com/passcode`)

Inspect the contents of `~/.cf/config.json` and copy the value of `RefreshToken`.

Paste the value as the value for `CF_REFRESH-TOKEN` in your `config/secrets.json`

```
{
  "CF_TOKEN-PROVIDER": "sso",
  "CF_API-HOST": "xxxxx",
  "CF_REFRESH-TOKEN": "xxxxx",
}
```

### using scripts

Please review the [manifest.yml](../manifest.yml) before deploying.

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

> Note: If you are seeing [OutOfMemory exceptions](https://dzone.com/articles/troubleshooting-problems-with-native-off-heap-memo) shortly after startup you may need to [cf scale](https://docs.run.tanzu.vmware.com/devguide/deploy-apps/cf-scale.html#vertical) the available memory for large foundations.  You may also need to adjust `JAVA_OPTS` environment variable and increase the amount of direct memory available via `-XX:MaxDirectMemorySize`.



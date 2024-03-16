#!\usr\bin\env pwsh

# Script assumes VMware Postgres for VMware Tanzu Application Service (https://docs.vmware.com/en/VMware-Postgres-for-VMware-Tanzu-Application-Service/1.1/postgres/index.html) is available as service in cf marketplace
# Feel free to swap out the service for other PostgreSQL providers, like:
#   * Meta Azure Service Broker - https://github.com/Azure/meta-azure-service-broker/blob/master/docs/azure-postgresql-db.md
#   * AWS Service Broker - http://docs.pivotal.io/aws-services/creating.html#rds

param (
    [string]$Provider = "--with-user-provided-service"
)

$AppName=cf-butler



switch ($Provider) {

	"--with-credhub" {
		cf push --no-start
		cf create-service credhub default $AppName-secrets -c config\secrets.json
        cf create-service postgres on-demand-postgres-db $AppName-backend
		while (cf service $AppName-secrets -notcontains "succeeded") {
			Write-Host "$APP_NAME-secrets is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf bind-service $AppName $AppName-secrets
		while (cf service $AppName-backend -notcontains "succeeded") {
			Write-Host "$APP_NAME-backend is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf start $AppName
	}

	"--with-user-provided-service" {
		cf push --no-start
		cf create-user-provided-service $AppName-secrets -p config\secrets.json
        cf create-service postgres on-demand-postgres-db $AppName-backend
		while (cf service $AppName-secrets -notcontains "succeeded") {
			Write-Host "$APP_NAME-secrets is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf bind-service $AppName $AppName-secrets
		while (cf service $AppName-backend -notcontains "succeeded") {
			Write-Host "$APP_NAME-backend is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf start $AppName
	}

}

#!\usr\bin\env pwsh

param (
    [string]$Provider = "--with-user-provided-service"
)

$AppName="cf-butler"



switch ($Provider) {

	"--with-credhub" {
		cf push --no-start
		cf create-service credhub default $AppName-secrets -c config\secrets.json
		while (cf service $AppName-secrets -notcontains "succeeded") {
			Write-Host "$APP_NAME-secrets is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf bind-service $AppName $AppName-secrets
		cf start $AppName
	}

	"--with-user-provided-service" {
		cf push --no-start
		cf create-user-provided-service $AppName-secrets -p config\secrets.json
		while (cf service $AppName-secrets -notcontains "succeeded") {
			Write-Host "$APP_NAME-secrets is not ready yet..."
			Start-Sleep -Seconds 5
		}
		cf bind-service $AppName $AppName-secrets
		cf start $AppName
	}

}

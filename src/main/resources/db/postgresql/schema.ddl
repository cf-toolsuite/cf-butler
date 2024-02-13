CREATE TABLE IF NOT EXISTS organizations ( id VARCHAR(50) PRIMARY KEY, org_name VARCHAR(250) NOT NULL );
CREATE TABLE IF NOT EXISTS spaces ( org_id VARCHAR(50) NOT NULL, space_id VARCHAR(50) NOT NULL, org_name VARCHAR(250) NOT NULL, space_name VARCHAR(250) NOT NULL, PRIMARY KEY(org_id, space_id) );
CREATE TABLE IF NOT EXISTS application_detail ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), app_name VARCHAR(100), buildpack VARCHAR(500), buildpack_version VARCHAR(50), image VARCHAR(250), stack VARCHAR(25), running_instances INT, total_instances INT, memory_used BIGINT, disk_used BIGINT, memory_quota BIGINT, disk_quota BIGINT, urls VARCHAR(512000), last_pushed TIMESTAMP, last_event VARCHAR(50), last_event_actor VARCHAR(100), last_event_time TIMESTAMP, requested_state VARCHAR(25), buildpack_release_type VARCHAR(100), buildpack_release_date TIMESTAMP, buildpack_latest_version VARCHAR(50), buildpack_latest_url VARCHAR(500) );
CREATE TABLE IF NOT EXISTS java_application_detail ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), app_name VARCHAR(100), droplet_id VARCHAR(100), pom_contents VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS service_instance_detail ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), service_instance_id VARCHAR(50), service_name VARCHAR(100), service VARCHAR(100), description VARCHAR(1000), plan VARCHAR(50), type VARCHAR(30), bound_applications VARCHAR(512000), last_operation VARCHAR(50), last_updated TIMESTAMP, dashboard_url VARCHAR(250), requested_state VARCHAR(25) );
CREATE TABLE IF NOT EXISTS application_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), operation VARCHAR(25), description VARCHAR(1000), state VARCHAR(25), options VARCHAR(512000), organization_whitelist VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS service_instance_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), operation VARCHAR(25), description VARCHAR(1000), options VARCHAR(512000), organization_whitelist VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS endpoint_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), description VARCHAR(1000), endpoints VARCHAR(512000), email_notification_template VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS query_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), description VARCHAR(1000), queries VARCHAR(512000), email_notification_template VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS hygiene_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), days_since_last_update INTEGER, operator_email_template VARCHAR(512000), notifyee_email_template VARCHAR(512000), organization_whitelist VARCHAR(512000), include_applications BOOLEAN, include_service_instances BOOLEAN )
CREATE TABLE IF NOT EXISTS legacy_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), stacks VARCHAR(250), service_offerings VARCHAR(100), operator_email_template VARCHAR(512000), notifyee_email_template VARCHAR(512000), organization_whitelist VARCHAR(512000) )
CREATE TABLE IF NOT EXISTS application_relationship ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), app_name VARCHAR(100), service_instance_id VARCHAR(50), service_name VARCHAR(100), service_offering VARCHAR(100), service_plan VARCHAR(50), service_type VARCHAR(30) );
CREATE TABLE IF NOT EXISTS historical_record ( pk BIGSERIAL PRIMARY KEY, transaction_date_time TIMESTAMP, action_taken VARCHAR(20), organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), service_instance_id VARCHAR(50), type VARCHAR(20), name VARCHAR(300) );
CREATE TABLE IF NOT EXISTS resource_notification_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), resource_email_template VARCHAR(512000), resource_email_metadata VARCHAR(512000), resource_whitelist VARCHAR(512000), resource_blacklist VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS space_users ( pk BIGSERIAL PRIMARY KEY, organization varchar(100), space varchar(100), auditors VARCHAR(512000), managers VARCHAR(512000), developers VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS time_keeper ( collection_time TIMESTAMP PRIMARY KEY );
CREATE OR REPLACE VIEW service_bindings AS select ad.pk, ad.organization, ad.space, ad.app_id, ar.service_instance_id, ad.app_name, ad.buildpack, ad.buildpack_version, ad.image, ad.stack, ad.running_instances, ad.total_instances, ad.memory_used, ad.disk_used, ad.urls, ad.last_pushed, ad.last_event, ad.last_event_actor, ad.last_event_time, ad.requested_state, ad.buildpack_release_type, ad.buildpack_release_date, ad.buildpack_latest_version, ad.buildpack_latest_url from application_detail ad left join application_relationship ar on ad.app_id = ar.app_id;

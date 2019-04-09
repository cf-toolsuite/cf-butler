CREATE TABLE IF NOT EXISTS organizations ( id VARCHAR(50) PRIMARY KEY, org_name VARCHAR(250) NOT NULL );
CREATE TABLE IF NOT EXISTS spaces ( org_name VARCHAR(250) NOT NULL, space_name VARCHAR(250) NOT NULL, PRIMARY KEY(org_name, space_name) );
CREATE TABLE IF NOT EXISTS application_detail ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50) NOT NULL UNIQUE, app_name VARCHAR(100), buildpack VARCHAR(50), image VARCHAR(250), stack VARCHAR(25), running_instances INT, total_instances INT, memory_used BIGINT, disk_used BIGINT, urls VARCHAR(512000), last_pushed TIMESTAMP, last_event VARCHAR(50), last_event_actor VARCHAR(100), last_event_time TIMESTAMP, requested_state VARCHAR(25) );
CREATE TABLE IF NOT EXISTS service_instance_detail ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), service_id VARCHAR(50) NOT NULL UNIQUE, service_name VARCHAR(100), service VARCHAR(100), description VARCHAR(1000), plan VARCHAR(50), type VARCHAR(30), bound_applications VARCHAR(512000), last_operation VARCHAR(50), last_updated TIMESTAMP, dashboard_url VARCHAR(250), requested_state VARCHAR(25) );
CREATE TABLE IF NOT EXISTS application_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), description VARCHAR(1000), state VARCHAR(25), from_datetime TIMESTAMP, from_duration VARCHAR(25), delete_services BOOLEAN, organization_whitelist VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS service_instance_policy ( pk BIGSERIAL PRIMARY KEY, id VARCHAR(50), description VARCHAR(1000), from_datetime TIMESTAMP, from_duration VARCHAR(25), organization_whitelist VARCHAR(512000) );
CREATE TABLE IF NOT EXISTS application_relationship ( pk BIGSERIAL PRIMARY KEY, organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), app_name VARCHAR(100), service_id VARCHAR(50), service_name VARCHAR(100), service_plan VARCHAR(50), service_type VARCHAR(30) );
CREATE TABLE IF NOT EXISTS historical_record ( pk BIGSERIAL PRIMARY KEY, transaction_datetime TIMESTAMP, action_taken VARCHAR(20), organization VARCHAR(100), space VARCHAR(100), app_id VARCHAR(50), service_id VARCHAR(50), type VARCHAR(20), name VARCHAR(300) );
CREATE TABLE IF NOT EXISTS space_users ( pk BIGSERIAL PRIMARY KEY, organization varchar(100), space varchar(100), auditors VARCHAR(512000), managers VARCHAR(512000), developers VARCHAR(512000) );
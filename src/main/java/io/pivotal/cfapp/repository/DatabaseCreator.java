package io.pivotal.cfapp.repository;

import java.sql.Connection;
import java.sql.SQLException;

import org.davidmoten.rx.jdbc.Database;
import org.davidmoten.rx.jdbc.exceptions.SQLRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;


@Profile("jdbc")
@Component
public class DatabaseCreator implements ApplicationRunner {

	private final Database database;

	@Autowired
	public DatabaseCreator(Database database) {
		this.database = database;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection c = database.connection().blockingGet()) {
			c.setAutoCommit(true);
			createAppDetailTable(c);
			createServiceDetailTable(c);
			createApplicationPolicyTable(c);
			createServiceInstancePolicyTable(c);
			createAppRelationshipTable(c);
			createHistoricalRecordTable(c);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
	}

	protected void createAppDetailTable(Connection c) throws SQLException {
        c.prepareStatement("create table app_detail ( id int identity primary key, organization varchar(100), space varchar(100), app_id varchar(50), app_name varchar(100), buildpack varchar(50), image varchar(250), stack varchar(25), running_instances int, total_instances int, urls varchar(2000), last_pushed timestamp, last_event varchar(50), last_event_actor varchar(100), last_event_time timestamp, requested_state varchar(25) )")
			.execute();
	}

	protected void createServiceDetailTable(Connection c) throws SQLException {
		c.prepareStatement("create table service_detail ( id int identity primary key, organization varchar(100), space varchar(100), service_id varchar(50), name varchar(100), service varchar(100), description varchar(1000), plan varchar(50), type varchar(30), bound_applications clob(20M), last_operation varchar(50), last_updated timestamp, dashboard_url varchar(250), requested_state varchar(25) )")
    		.execute();
	}

	protected void createApplicationPolicyTable(Connection c) throws SQLException {
		c.prepareStatement("create table application_policy ( description varchar(1000), state varchar(25), from_datetime timestamp, from_duration varchar(25), delete_services boolean )")
			.execute();
		
	}
	
	protected void createServiceInstancePolicyTable(Connection c) throws SQLException {
		c.prepareStatement("create table service_instance_policy ( description varchar(1000), from_datetime timestamp, from_duration varchar(25) )")
			.execute();
	}
	
	protected void createAppRelationshipTable(Connection c) throws SQLException {
		c.prepareStatement("create table app_relationship ( organization varchar(100), space varchar(100), app_id varchar(50), app_name varchar(100), service_id varchar(50), service_name varchar(100), service_plan varchar(50), service_type varchar(30) )")
			.execute();
	}
	
	protected void createHistoricalRecordTable(Connection c) throws SQLException {
		c.prepareStatement("create table historical_record ( datetime_removed timestamp, organization varchar(100), space varchar(100), id varchar(50), type varchar(20), name varchar(500) )")
			.execute();
	}
	
}

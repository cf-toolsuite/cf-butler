package io.pivotal.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

//import reactor.blockhound.BlockHound;
//import reactor.tools.agent.ReactorDebugAgent;


@SpringBootApplication
@ConfigurationPropertiesScan
public class AppInit {

	public static void main(String[] args) {
		//ReactorDebugAgent.init();
		//BlockHound.install();
		SpringApplication.run(AppInit.class, args);
	}
}
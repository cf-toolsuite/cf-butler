package io.pivotal.cfapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
public class AppInit {

	public static void main(String[] args) {
		ReactorDebugAgent.init();
		SpringApplication.run(AppInit.class, args);
	}
}
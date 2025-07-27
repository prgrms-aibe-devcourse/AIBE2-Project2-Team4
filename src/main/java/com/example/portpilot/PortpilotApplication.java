package com.example.portpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PortpilotApplication {

	public static void main(String[] args) {
		SpringApplication.run(com.example.portpilot.PortpilotApplication.class, args);
	}

}

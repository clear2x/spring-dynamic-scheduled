package com.clear2x.spring_dynamic_scheduled;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringDynamicScheduledApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringDynamicScheduledApplication.class, args);
	}

}

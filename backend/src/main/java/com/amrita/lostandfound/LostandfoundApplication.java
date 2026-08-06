package com.amrita.lostandfound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.PropertySource;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
public class LostandfoundApplication {

	public static void main(String[] args) {
		SpringApplication.run(LostandfoundApplication.class, args);
	}

}

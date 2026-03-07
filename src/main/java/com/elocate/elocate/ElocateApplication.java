package com.elocate.elocate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ElocateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElocateApplication.class, args);
	}

}

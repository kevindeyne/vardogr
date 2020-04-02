package com.kevindeyne.datascrambler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataScramblerApplication {

	public static void main(String[] args) {
		System.getProperties().setProperty("org.jooq.no-logo", "true");
		SpringApplication.run(DataScramblerApplication.class, args);
	}
}

package com.example.CargoAssign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CargoAssignApplication {

	public static void main(String[] args) {
		normalizeDbUrlForJdbc();
		SpringApplication.run(CargoAssignApplication.class, args);
	}

	private static void normalizeDbUrlForJdbc() {
		String dbUrl = System.getenv("DB_URL");
		if (dbUrl == null || dbUrl.isBlank()) {
			return;
		}

		if (dbUrl.startsWith("postgresql://")) {
			System.setProperty("DB_URL", "jdbc:" + dbUrl);
			return;
		}

		if (dbUrl.startsWith("postgres://")) {
			System.setProperty("DB_URL", "jdbc:postgresql://" + dbUrl.substring("postgres://".length()));
		}
	}
}

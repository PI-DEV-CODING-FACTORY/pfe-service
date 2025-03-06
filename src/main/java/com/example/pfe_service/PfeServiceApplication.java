package com.example.pfe_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class PfeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PfeServiceApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner alterPfeTableColumns(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// Alter the column types for URL fields in the Pfe table
				jdbcTemplate.execute("ALTER TABLE pfe ALTER COLUMN rapport_url TYPE TEXT");
				jdbcTemplate.execute("ALTER TABLE pfe ALTER COLUMN github_url TYPE TEXT");
				jdbcTemplate.execute("ALTER TABLE pfe ALTER COLUMN video_url TYPE TEXT");
				
				System.out.println("Successfully altered Pfe table columns to TEXT type");
			} catch (Exception e) {
				System.err.println("Error altering Pfe table columns: " + e.getMessage());
				// Don't fail startup if the columns are already of type TEXT
			}
		};
	}
}

package com.example.fitnessWeb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FitnessWebApplication {
	public static void main(String[] args) {
		SpringApplication.run(FitnessWebApplication.class, args);
	}
}
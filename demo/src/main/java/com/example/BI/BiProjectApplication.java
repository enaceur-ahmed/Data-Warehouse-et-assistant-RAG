package com.example.BI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.BI"})
public class BiProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(BiProjectApplication.class, args);
	}
}
package com.datn.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		// Set timezone JVM = Việt Nam trước khi Spring Boot khởi động
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(BackendApplication.class, args);
	}

}

package com.taller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.scheduling.annotation.EnableScheduling;

@RequestMapping("/taller")
@EnableScheduling
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class TallerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TallerApplication.class, args);
	}
}

package com.sergiovitorino.springwebfluxjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching(proxyTargetClass = true)
public class Start {

	public static void main(String[] args) {
		SpringApplication.run(Start.class, args);
	}

}

package com.sk.customer;

import com.sk.customer.dto.WeatherConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(WeatherConfigProperties.class)
public class CustomerAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerAiApplication.class, args);
	}

}

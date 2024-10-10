package com.sk.customer;

import org.springframework.boot.SpringApplication;

public class TestCustomerAiApplication {

	public static void main(String[] args) {
		SpringApplication.from(CustomerAiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

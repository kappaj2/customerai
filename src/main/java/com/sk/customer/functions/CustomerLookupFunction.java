package com.sk.customer.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.sk.customer.service.customer.CustomerManagementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class CustomerLookupFunction {

     private final CustomerManagementService customerManagementService;

     public CustomerLookupFunction(CustomerManagementService customerManagementService) {
          this.customerManagementService = customerManagementService;
     }

     @Bean
     @Description("Get the customer information for a provided msisdn.")
     public Function<JsonNode, String> getCustomerFunction() {
          return customerManagementService;
     }
}

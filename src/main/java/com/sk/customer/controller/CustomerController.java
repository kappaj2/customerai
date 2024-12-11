package com.sk.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.sk.customer.service.customer.CustomerManagementService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

     public final CustomerManagementService customerManagementService;
     private final ChatClient chatClient;

     public CustomerController(CustomerManagementService customerManagementService,
                               ChatClient.Builder builder) {
          this.customerManagementService = customerManagementService;

          this.chatClient = builder
                  .defaultSystem("You are a helpful AI Assistant that can answer questions about customers.")
                  .defaultFunctions("getCustomerFunction")
                  .build();
     }

     @GetMapping("/{msisdn}")
     public JsonNode getCustomer(@PathVariable String msisdn) {
          return customerManagementService.retrieveCustomerByMsisdn(msisdn);
     }


     @GetMapping("/function")
     public String getCustomerFunction(@RequestParam String question) {

          return chatClient.prompt()
                  .user(question)
                  .call()
                  .content();
     }

}
/*
     private final ChatClient chatClient;

     public CityController(ChatClient.Builder builder) {
          this.chatClient = builder
                  .defaultSystem("You are a helpful AI Assistant answering questions about cities around the world.")
                  .defaultFunctions("currentWeatherFunction")
                  .build();
     }
 */
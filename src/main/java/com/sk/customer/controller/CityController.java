package com.sk.customer.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cities")
public class CityController {

     private final ChatClient chatClient;

     public CityController(ChatClient.Builder builder) {
          this.chatClient = builder
                  .defaultSystem("You are a helpful AI Assistant answering questions about cities around the world.")
                  .defaultFunctions("currentWeatherFunction")
                  .build();
     }

     @GetMapping
     public String cityFaq(@RequestParam String message) {
          return chatClient.prompt()
                  .user(message)
                  .call()
                  .content();
     }
}

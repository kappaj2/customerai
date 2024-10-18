package com.sk.customer.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/actor")
public class ActorController {

     private final ChatClient chatClient;

     public ActorController(ChatClient.Builder builder) {
          this.chatClient = builder.build();
     }

     record ActorFilms(String actor, List<String> films) {
     }

     @GetMapping("/films")
     public ActorFilms getActorFilms(@RequestParam String actor) {

          var promptTemplate = new PromptTemplate("Generate the filmography for the actor {actor}. Return the top 10 films.");
          var prompt = promptTemplate.create(Map.of("actor", actor));

          return chatClient.prompt(prompt)
                  .call()
                  .entity(ActorFilms.class);
     }
}

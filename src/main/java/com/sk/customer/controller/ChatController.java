package com.sk.customer.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
     private final ChatClient chatClient;

     public ChatController(ChatClient.Builder builder,
                           VectorStore vectorStore) {
          this.chatClient = builder
                  .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                  .build();
     }

     @PostMapping()
     public String ollamaChat(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .call()
                  .content();
     }

     @GetMapping("/stream")
     public Flux<String> ollamaChatStream(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .stream()
                  .content();
     }

     @GetMapping("/joke/template")
     public String jokeTemplate(@RequestParam(value = "topic", defaultValue = "dogs") String topic) {

          var promptTemplate = new PromptTemplate("Tell me a dad joke about topic {topic}");
          var prompt = promptTemplate.create(Map.of("topic", topic));

          return chatClient.prompt(prompt)
                  .call()
                  .content();

     }
}

package com.sk.customer.controller;

import com.sk.customer.service.CustomerService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.crypto.dsig.keyinfo.PGPData;
import java.util.List;
import java.util.Map;

@RestController
public class OllamaController {

     private final ChatClient chatClient;
     private final VectorStore vectorStore;
     private final CustomerService customerService;

     public OllamaController(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             CustomerService customerService) {
          this.chatClient = builder.build();
          this.vectorStore = vectorStore;
          this.customerService = customerService;
     }



     //   https://www.youtube.com/watch?v=TPcqBuxl5B8&t=921s
     //   https://stackoverflow.com/questions/79070131/mistral-model-not-found-issue-in-spring-ai


     @GetMapping("/vector")
     public List<Document> vector(@RequestParam String message) {
          customerService.addCustomer(message);

          return vectorStore.similaritySearch(message);
     }

     @GetMapping("/ollama/joke/template")
     public String jokeTemplate(@RequestParam(value = "topic", defaultValue = "dogs") String topic) {

          PromptTemplate promptTemplate = new PromptTemplate("Tell me a dad joke about topic {topic}");
          Prompt prompt = promptTemplate.create(Map.of("topic", topic));

          return chatClient.prompt(prompt)
                  .call()
                  .content();

     }

     @GetMapping("/ollama/joke")
     public String joke(){

          return chatClient.prompt()
                  .user("Please tell me a dad joke about dogs. Do not use the ruff one.")
                  .call()
                  .content();
     }
}

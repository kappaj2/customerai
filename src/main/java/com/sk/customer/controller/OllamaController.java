package com.sk.customer.controller;

import com.sk.customer.advisor.SimpleLoggerAdvisor;
import com.sk.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ollama")
public class OllamaController {

     @Value("classpath:images/HoverCraftBeginning.png")
     private Resource imageResource;

     private final ChatClient chatClient;
     private final VectorStore vectorStore;
     private final CustomerService customerService;
     private final ChatModel chatModel;

     public OllamaController(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             CustomerService customerService,
                             ChatModel chatModel) {

          //https://spring.io/blog/2024/10/02/supercharging-your-ai-applications-with-spring-ai-advisors
          this.chatClient = builder
                  //.defaultSystem("You are an assistant that that helps with customer registrations. You will provide as much information as possible based upon that data provided. If you cannot find any information, just say so. ")
                      .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                  .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                  .build();
          this.vectorStore = vectorStore;
          this.customerService = customerService;
          this.chatModel = chatModel;
     }


     @GetMapping("/images")
     public String analyzeImage() {
          return chatClient.prompt()
                  .user(userSpec -> userSpec.text("Please analyze the image and tell me what you see")
                          .media(MimeTypeUtils.IMAGE_PNG, (org.springframework.core.io.Resource) imageResource))
                  .call()
                  .content();
     }
     /*

          Prompt with parameters

          public String jokeTemplate(@RequestParam(value = "topic", defaultValue = "dogs") String topic) {
          return chatClient.prompt()
                    .user(userSpec -> userSpec.text("Tell me a joke about {topic}"),
                              .param("topic", topic))
                    .call()
                    .content();

      */

     @GetMapping("/joke/template")
     public String jokeTemplate(@RequestParam(value = "topic", defaultValue = "dogs") String topic) {

          var promptTemplate = new PromptTemplate("Tell me a dad joke about topic {topic}");
          var prompt = promptTemplate.create(Map.of("topic", topic));

          return chatClient.prompt(prompt)
                  .call()
                  .content();

     }

     @PostMapping("/chat")
     public String ollamaChat(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .call()
                  .content();
     }

     @GetMapping("/chat/stream")
     public Flux<String> ollamaChatStream(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .stream()
                  .content();
     }

     @GetMapping("/question")
     public String askQuestion(HttpSession session,
                               @RequestParam(required = false) String question,
                               @RequestParam(required = false) String persona) {

          if (persona == null || persona.isBlank()) {

               // Here we can use the default ChatClient as that has the default advisor and other stuff configured.
               return chatClient.prompt()

//                  .advisors(advisorSpec -> {
//                       advisorSpec.params(Map.of("chat_memory_conversation_id", session.getId(), "chat_memory_response_size", "20"));
//                  })
                       .advisors(new SimpleLoggerAdvisor())
                       .user(question)
                       .call()
                       .content();

          } else {

               // Build a new chat client with a default system configuration.
               var client = ChatClient.builder(chatModel)
                       .defaultSystem("You are a friendly assistance that answers questions using the following persona: {persona}")
                       .build();

               return client.prompt()
                       .system(sp -> sp.param("persona", persona))
                       .user(question)
                       .call()
                       .content();
          }


     }
}

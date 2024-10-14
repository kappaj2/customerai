package com.sk.customer.controller;

import com.sk.customer.advisor.SimpleLoggerAdvisor;
import com.sk.customer.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class OllamaController {

     //@Value("classpath:images/AWS-Systems-Overview.png")
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
          this.chatClient = builder
                  // .defaultSystem("You are a serious engineer talking to a super nerd")
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


        Image processing:

        @Value("classpath:/images/spring-logo.png")
        private Resource imageResource;

        public String analyzeImage() {
          return chatClient.prompt()
                    .user(userSpec -> userSpec.text("Tell me about the image"),
                              .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                    .call()
                    .content();
      */

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

     @PostMapping("/ollama/chat")
     public String ollamaChat(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .call()
                  .content();
     }

     @GetMapping("/ollama/chat/stream")
     public Flux<String> ollamaChatStream(@RequestParam String message) {

          return chatClient.prompt()
                  .user(message)
                  .stream()
                  .content();
     }

     @GetMapping("/ollama/question")
     public String askQuestion(HttpSession session, @RequestParam String question) {

          String prompt = "Answer the following question: " + question;
          return chatClient.prompt()
                  .advisors(advisorSpec -> {
                       advisorSpec.params(Map.of("chat_memory_conversation_id", session.getId(), "chat_memory_response_size", "20"));
                  })
                  .advisors(new SimpleLoggerAdvisor())
                  .user(prompt)
                  .call()
                  .content();


     }
}

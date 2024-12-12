package com.sk.customer.controller;

import com.sk.customer.advisor.SimpleLoggerAdvisor;
import com.sk.customer.dto.ChatRequest;
import com.sk.customer.dto.ChatResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/api/v1/ollama")
public class OllamaController {

     private final ChatClient chatClient;
     private final ChatModel chatModel;

     public OllamaController(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             ChatModel chatModel) {

          var searchRequest = SearchRequest
                  .defaults()
                  .withTopK(5);

          this.chatClient = builder
                  .defaultSystem("You are an assistant that helps customers to answer queries about their registration data. The customer data is in the format of raw JSON payloads, so you need to extract the node and value properties to look at the available data. If you cannot find any information, just say so. ")
                  //.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                  //.defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                  .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, searchRequest))
                  .defaultOptions(ChatOptionsBuilder.builder().withTemperature(0.8).build())
                  .build();
          this.chatModel = chatModel;
     }

     @GetMapping("/question")
     public ChatResponse askPersonaQuestion(@RequestBody @NotNull ChatRequest chatRequest) {

          // Build a new chat client with a default system configuration.
          // Exclude all the memory advisors, etc
          var client = ChatClient.builder(chatModel)
                  .defaultSystem("You are a friendly assistance that answers questions using the following persona: {persona}")
                  .build();

          var content = client.prompt()
                  .system(sp -> sp.param("persona", chatRequest.getPersona()))
                  .user(chatRequest.getQuestion())
                  .call()
                  .content();

          return ChatResponse.builder()
                  .conversationId(chatRequest.getConversationId())
                  .responseContent(content)
                  .build();
     }

     @PostMapping("/question")
     public ChatResponse askQuestion(@RequestBody @NotNull ChatRequest chatRequest) {
          if (chatRequest.getConversationId() == null || chatRequest.getConversationId().isEmpty()) {
               chatRequest.setConversationId(UUID.randomUUID().toString());
          }

          // Here we can use the default ChatClient as that has the default advisor and other stuff configured.
          var content = chatClient.prompt()
//                  .advisors(advisorSpec -> {
//                       advisorSpec.params(Map.of(CHAT_MEMORY_CONVERSATION_ID_KEY, chatRequest.getConversationId(),
//                               CHAT_MEMORY_RETRIEVE_SIZE_KEY, "20"));
//                  })
//                  .advisors(new SimpleLoggerAdvisor())
                  .user(chatRequest.getQuestion())
                  .call()
                  .content();
          return ChatResponse.builder()
                  .conversationId(chatRequest.getConversationId())
                  .responseContent(content)
                  .build();
     }
}

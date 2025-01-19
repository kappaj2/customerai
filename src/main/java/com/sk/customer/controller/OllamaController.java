package com.sk.customer.controller;

import com.sk.customer.advisor.SimpleLoggerAdvisor;
import com.sk.customer.dto.ChatRequest;
import com.sk.customer.dto.ChatResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
@RequestMapping("/api/v1/ollama")
public class OllamaController {

     private final ChatClient chatClient;
     private final ChatModel chatModel;
     private final VectorStore vectorStore;

     @Value("classpath:/templates/rag-prompt-template.st")
     private Resource ragPromptTemplate;

     public OllamaController(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             ChatModel chatModel) {
          this.vectorStore = vectorStore;

          String queryText = "Customer profile with mobile number " + "27782014637" +
                  " including their service history and account details";

          var searchRequest = SearchRequest.builder()
                  .query(queryText)
                  .filterExpression("msisdn == '27782014637'")
                  .topK(2)
                  .build();


          this.chatClient = builder
                  .defaultSystem("You are an assistant that helps customers to answer queries about their registration data. If you cannot find any information, just say so. ")
                  .defaultAdvisors(new SimpleLoggerAdvisor())
                  .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
//                  .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
//                  .defaultAdvisors(new PromptChatMemoryAdvisor(new InMemoryChatMemory()))
                  .defaultOptions(ChatOptions.builder().temperature(0.8).build())
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

          /*

           */

//          var searchRequest = SearchRequest
//                  .defaults()
//                  .withQuery(question)
//                  .withTopK(3);  // Retrieve top 3 most relevant documents
//
//          // 2. Perform similarity search
//          List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
//
//          // 3. Create the prompt with retrieved context
//          String context = relevantDocs.stream()
//                  .map(Document::getContent)
//                  .collect(Collectors.joining("\n\n"));
//
//          // 4. Set up the QA prompt using the QuestionAnswerPromptTemplate
//          var promptTemplate = QuestionAnswerPromptTemplate
//                  .withSystemMessage("You are a helpful assistant. Use only the provided context to answer questions. If you cannot answer from the context, say so.")
//                  .withHumanMessageTemplate("Context:\n" + context + "\n\nQuestion: {question}");
//
//          // 5. Create the completion request
//          var completionRequest = CompletionRequest.builder()
//                  .promptTemplate(promptTemplate)
//                  .withParameter("question", question)
//                  .build();
//
//          // 6. Get the response using the AI client
//          var response = aiClient.generate(completionRequest);
//

          /*

           */
          // Here we can use the default ChatClient as that has the default advisor and other stuff configured.
          var content = chatClient.prompt()
                  .advisors(advisorSpec -> {
                       advisorSpec.params(Map.of(CHAT_MEMORY_CONVERSATION_ID_KEY, chatRequest.getConversationId(),
                               CHAT_MEMORY_RETRIEVE_SIZE_KEY, "20"));
                  })
                  .user(chatRequest.getQuestion())
                  .call()
                  .content();

          return ChatResponse.builder()
                  .conversationId(chatRequest.getConversationId())
                  .responseContent(content)
                  .build();
     }


     /**
      * This method with a PromptTemplate is a lot more consistent than the Advisors. Looks like the advisors really suffer from sequencing
      * which result in the payload bing passed to the LLM empty with not RAG documents.
      *
      * @param chatRequest
      * @return
      */
     @PostMapping("/question3")
     public Answer getAnswer(@RequestBody @NotNull ChatRequest chatRequest) {

          String queryText = "Customer profile with mobile number " + "27782014637" +
                  " including their service history and account details";

          /*
               Supported filter operators include:
               Comparison: =, !=, >, <, >=, <=
               Logical: AND, OR, NOT
               Parentheses for grouping conditions
           */
          var searchRequest = SearchRequest.builder()
                  .query(queryText)
                  .filterExpression("msisdn == '27782014637'")
                  .topK(2)
                  .build();

          List<Document> documents = vectorStore.similaritySearch(searchRequest);

          List<String> contentList = documents.stream().map(Document::getContent).toList();

          PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
          Prompt prompt = promptTemplate.create(Map.of("input", chatRequest.getQuestion(), "documents",
                  String.join("\n", contentList)));

          contentList.forEach(System.out::println);

          org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);

          return new Answer(response.getResult().getOutput().getContent());
     }

     public record Answer(String answer) {
     }
}

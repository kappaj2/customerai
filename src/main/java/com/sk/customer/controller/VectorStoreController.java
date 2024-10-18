package com.sk.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.customer.service.CustomerService;
import com.sk.customer.service.JsonVectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vector")
@RequiredArgsConstructor
public class VectorStoreController {

     @Value("classpath:customer_data.json")
     private Resource jsonResource;

     private final JsonVectorStoreService jsonVectorStoreService;
     private final ObjectMapper objectMapper;
     private final CustomerService customerService;
     private final VectorStore vectorStore;

     @GetMapping("/add")
     public List<Document> vector(@RequestParam String message) {
          customerService.addTextDAta(message);

          return vectorStore.similaritySearch(message);
     }

     @GetMapping("/query")
     public String getQueryResults(@RequestParam String query){
          return jsonVectorStoreService.queryJSONVector(query).toString();//get(0).getContent();
     }

     @PostMapping("/loadjson/object")
     public ResponseEntity<String> loadJsonData(@RequestBody String payload){
          try {
               JsonNode jsonNode = objectMapper.readTree(payload);
               jsonVectorStoreService.loadJsonIntoVectorStore(jsonNode);
               return ResponseEntity.ok("Loaded");
          } catch (Exception e) {
               return ResponseEntity.badRequest().body("Error processing JSON: " + e.getMessage());
          }

     }

     @GetMapping("/loadjson/file")
     public ResponseEntity<?> loadJsonFile() throws Exception{

          jsonVectorStoreService.addCustomerData(jsonResource);
          return ResponseEntity.ok("Loaded");
     }
}

package com.sk.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.customer.service.CustomerService;
import com.sk.customer.service.VectorStoreService;
import com.sk.customer.service.filehandling.DocumentProcessManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/vector")
@RequiredArgsConstructor
public class VectorStoreController {

     @Value("classpath:customer_data.json")
     private Resource jsonResource;

     private final VectorStoreService vectorStoreService;
     private final ObjectMapper objectMapper;
     private final CustomerService customerService;
     private final VectorStore vectorStore;
     private final DocumentProcessManager documentProcessorManager;

     @GetMapping("/add/text")
     public List<Document> vector(@RequestParam String message) {
          customerService.addTextDAta(message);

          return vectorStore.similaritySearch(message);
     }

     @GetMapping("/query")
     public String getQueryResults(@RequestParam String query) {
          return vectorStoreService.queryJSONVector(query).toString();//get(0).getContent();
     }

     @PostMapping("/loadjson/object")
     public ResponseEntity<String> loadJsonData(@RequestBody String payload) {
          try {
               JsonNode jsonNode = objectMapper.readTree(payload);
               vectorStoreService.loadJsonIntoVectorStore(jsonNode);
               return ResponseEntity.ok("Loaded");
          } catch (Exception e) {
               return ResponseEntity.badRequest().body("Error processing JSON: " + e.getMessage());
          }

     }

     @GetMapping("/loadjson/file")
     public ResponseEntity<?> loadJsonFile() throws Exception {

          vectorStoreService.addCustomerData(jsonResource);
          return ResponseEntity.ok("Loaded");
     }

     @PostMapping("/file/upload")
     public ResponseEntity<?> readFile(@RequestParam("file") @Valid MultipartFile file, @RequestParam("keywordsCount") int keywordsCount) throws IOException {
          log.debug("received file {} for processing, keywordsCount: {}", file.getName(), keywordsCount);

          byte[] bytes = file.getBytes();
          Path path = Paths.get("/tmp/" + file.getOriginalFilename());
          Files.write(path, bytes);

          return ResponseEntity.ok(documentProcessorManager.process(file, keywordsCount));
     }
}

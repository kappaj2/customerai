package com.sk.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

     private final VectorStore vectorStore;

     public void addCustomer(String customerName) {
          var document = new Document(customerName, Map.of("customerId", 100L));
          vectorStore.add(List.of(document));
     }
}

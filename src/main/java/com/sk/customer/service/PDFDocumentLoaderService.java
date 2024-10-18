package com.sk.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PDFDocumentLoaderService {

     private final VectorStore vectorStore;

     public void loadPDFDocument(String document) {
          //TODO: Load PDF Document


     }
}

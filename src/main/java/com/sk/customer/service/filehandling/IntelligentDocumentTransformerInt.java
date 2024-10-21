package com.sk.customer.service.filehandling;

import org.springframework.ai.document.Document;

import java.util.List;

public interface IntelligentDocumentTransformerInt {
     
     String keywordsEnricher(List<Document> documents, int keywordsCount);

     List<Document> textSplitter(List<Document> documents);

     String summaryEnricher(List<Document> documents);
}

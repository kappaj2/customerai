package com.sk.customer.service.filehandling;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IntelligentDocumentTransformer implements IntelligentDocumentTransformerInt {

     private ChatModel chatModel;

     public IntelligentDocumentTransformer(@NotNull ChatModel chatModel) {
          this.chatModel = chatModel;
     }

     @NotNull
     @Override
     public String keywordsEnricher(List<Document> documents, int keywordsCount) {
          log.info("Received documents for keyword enriching, size: {}", documents.size());
          KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, keywordsCount);
          List<Document> enrichedDocs = enricher.apply(documents);
          log.info("Successfully processed keywords enriching");
          return enrichedDocs.stream()
                  .map(doc -> doc.getMetadata().get("excerpt_keywords"))
                  .toString();
     }

     @Override
     public String summaryEnricher(List<Document> documents) {
          return null;
     }

     @NotNull
     @Override
     public List<Document> textSplitter(List<Document> documents) {
          return new TokenTextSplitter()
                  .apply(documents);
     }
}

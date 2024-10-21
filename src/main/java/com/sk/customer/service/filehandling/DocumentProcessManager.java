package com.sk.customer.service.filehandling;

import com.sk.customer.dto.ProcessResponse;
import com.sk.customer.dto.ReadResponse;
import com.sk.customer.exceptions.FileProcessingFailureException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class DocumentProcessManager {

     private final DocReaderServiceInt documentReader;
     private final IntelligentDocumentTransformerInt intelligentDocumentTransformer;
     private final DocWriterServiceInt documentWriterService;

     public DocumentProcessManager(DocReaderServiceInt documentReader, IntelligentDocumentTransformerInt intelligentDocumentTransformer, DocWriterServiceInt documentWriterService) {
          this.documentReader = documentReader;
          this.intelligentDocumentTransformer = intelligentDocumentTransformer;
          this.documentWriterService = documentWriterService;
     }

     @NotNull
     public ProcessResponse process(MultipartFile file, int keywordsCount) {
          log.info("Processing of file: {} has been started", file.getOriginalFilename());

          try {
               ReadResponse readResponse = documentReader.readFile(file);

               String keywords = intelligentDocumentTransformer.keywordsEnricher(readResponse.documents(), keywordsCount);
               //String keywords = "Testing";

               log.info("Extracted keywords - writing to vector store");
               // write to vector store
               documentWriterService.persist(readResponse.documents());

               log.debug("Done writing to vector store");
               return new ProcessResponse(keywords, "File Processed Successfully", readResponse);

          } catch (Exception e) {
               log.error("Processing failed due to {}", e.getMessage());
               throw new FileProcessingFailureException(e);
          }
     }
}

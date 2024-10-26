package com.sk.customer.service.filehandling;

import com.sk.customer.dto.ProcessResponse;
import com.sk.customer.dto.ReadResponse;
import com.sk.customer.exceptions.FileProcessingFailureException;
import com.sk.customer.persistence.repository.VectorStoreRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocumentProcessManager {

     private final DocReaderServiceInt documentReader;
     private final IntelligentDocumentTransformerInt intelligentDocumentTransformer;
     private final DocWriterServiceInt documentWriterService;
     private final VectorStoreRepository vectorStoreRepository;

     public DocumentProcessManager(DocReaderServiceInt documentReader, IntelligentDocumentTransformerInt intelligentDocumentTransformer, DocWriterServiceInt documentWriterService, VectorStoreRepository vectorStoreRepository) {
          this.documentReader = documentReader;
          this.intelligentDocumentTransformer = intelligentDocumentTransformer;
          this.documentWriterService = documentWriterService;
          this.vectorStoreRepository = vectorStoreRepository;
     }

     @NotNull
     public ProcessResponse process(MultipartFile file, int keywordsCount) {
          log.info("Processing of file: {} has been started", file.getOriginalFilename());

          try {
               ReadResponse readResponse = documentReader.readFile(file);

               //String keywords = intelligentDocumentTransformer.keywordsEnricher(readResponse.documents(), keywordsCount);
               String keywords = "Testing";

               log.info("Extracted keywords - writing to vector store");

               String fileName = readResponse.documents().get(0).getMetadata().get("file_name").toString();

               List<String> entityIdList = vectorStoreRepository.findAll()
                       .stream()
                       .filter(entity -> fileName.equals(entity.getMetadata().get("file_name")))
                       .map(entity -> entity.getId().toString())
                       .collect(Collectors.toList());

               // write to vector store
               if (!entityIdList.isEmpty()){
                    documentWriterService.delete(entityIdList);
               }

               documentWriterService.persist(readResponse.documents());

               log.debug("Done writing to vector store");
               return new ProcessResponse(keywords, "File Processed Successfully", readResponse);

          } catch (Exception e) {
               log.error("Processing failed due to {}", e.getMessage());
               throw new FileProcessingFailureException(e);
          }
     }
}

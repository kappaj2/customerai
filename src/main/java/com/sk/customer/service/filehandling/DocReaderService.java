package com.sk.customer.service.filehandling;

import com.sk.customer.dto.ReadResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DocReaderService implements DocReaderServiceInt {

     @NotNull
     @Override
     public ReadResponse readFile(@NotNull final MultipartFile file) throws Exception {

          var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          var todayDate = dateTimeFormatter.format(LocalDate.now());

          //var fileIdentifier = todayDate + "/" + FileHandlingUtils.formatFileName(Objects.requireNonNull(file.getOriginalFilename()));

          var fileIdentifier = FileHandlingUtils.formatFileName(Objects.requireNonNull(file.getOriginalFilename()));


          // based on the file type we will use the corresponding Document Reader from Spring AI
          Class<? extends DocumentReader> correspondingDocumentReader = FileHandlingUtils.getCorrespondingDocumentReader(file);

          // dynamic reader
//          DocumentReader documentReader = correspondingDocumentReader
//                  .getDeclaredConstructor(String.class)
//                  .newInstance("//tmp/"+file.getOriginalFilename());


          //          // Dedicated reader gives more capabilities/options
          PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                  .withPagesPerDocument(1)
                  .build();

          Path path = Paths.get("/tmp/" + file.getOriginalFilename());


          PagePdfDocumentReader reader = new PagePdfDocumentReader(path.toUri().toString(), config);
          TokenTextSplitter splitter = new TokenTextSplitter();
          List<Document> split = splitter.split(reader.read());


          //var documents = documentReader.read();
          log.info("Complete document reading..");

          return new ReadResponse(split,
                  split.stream()
                          .map(Document::getFormattedContent)
                          .collect(Collectors.joining())
          );
     }
}

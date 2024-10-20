package com.sk.customer.service.filehandling;

import org.apache.tika.Tika;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileHandlingUtils {

     public static String formatFileName(String fileName) {
          int dotIndex = fileName.lastIndexOf(".");
          String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
          String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);
          String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
          return baseName + "_" + timestamp + extension;
     }

     public static Class<? extends DocumentReader> getCorrespondingDocumentReader(MultipartFile file) {
          Tika tika = new Tika();
          try {
               return switch (tika.detect(file.getBytes())) {
                    case "application/pdf" -> PagePdfDocumentReader.class;
                    case "text/plain" -> TextReader.class;
                    default -> TikaDocumentReader.class;
               };
          } catch (IOException e) {
               throw new RuntimeException("Unable to detect the file type");
          }
     }
}

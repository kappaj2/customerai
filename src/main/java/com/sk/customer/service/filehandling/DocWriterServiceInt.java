package com.sk.customer.service.filehandling;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocWriterServiceInt {
     void persist(List<Document> documents);
}

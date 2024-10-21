package com.sk.customer.dto;


import org.springframework.ai.document.Document;

import java.util.List;

public record ReadResponse(List<Document> documents, String content) {
}

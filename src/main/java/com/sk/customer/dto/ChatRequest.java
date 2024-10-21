package com.sk.customer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {
     private @NotNull String question;
     private String conversationId;
     private String persona;
}

package com.sk.customer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponse {
     private String conversationId;
     private String responseContent;
}

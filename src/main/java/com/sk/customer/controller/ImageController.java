package com.sk.customer.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

     private final ChatClient chatClient;

     @Value("classpath:images/HoverCraftBeginning.png")
     private Resource imageResource;

     public ImageController(ChatClient.Builder builder) {
          this.chatClient = builder.build();
     }

     @GetMapping("/analyze")
     public String analyzeImage() {

          return chatClient.prompt()
                  .user(userSpec -> userSpec.text("Please analyze the image and tell me what you see")
                          .media(MimeTypeUtils.IMAGE_PNG, (org.springframework.core.io.Resource) imageResource))
                  .call()
                  .content();
     }
}

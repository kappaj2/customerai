package com.sk.customer.controller;

import com.sk.customer.dto.FaceDetectionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
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
          this.chatClient = builder.defaultOptions(ChatOptionsBuilder.builder()
                          .withTemperature(0.3)
                          .build())
                  .build();
     }

     @GetMapping("/analyze")
     public ResponseEntity<FaceDetectionResponse> analyzeImage() {
          String prompt = "Can you see a human face in this passport document image? " +
                  "Please respond with only 'yes' or 'no'.";

          String resp = chatClient.prompt()
                  .user(userSpec -> userSpec.text(prompt)
                          .media(MimeTypeUtils.IMAGE_PNG, (org.springframework.core.io.Resource) imageResource))
                  .call()
                  .content();

          boolean faceDetected = resp.toLowerCase().contains("yes");

          var rec = new FaceDetectionResponse(faceDetected, resp);

          return ResponseEntity.ok(rec);
     }

     @GetMapping("/analyze2")
     public ResponseEntity<String> analyzeImage2() {
          String prompt = "Tell met what you see in this photo.";

          String resp = chatClient.prompt()
                  .user(userSpec -> userSpec.text(prompt)
                          .media(MimeTypeUtils.IMAGE_PNG, (org.springframework.core.io.Resource) imageResource))
                  .call()
                  .content();
          return ResponseEntity.ok(resp);
     }
}

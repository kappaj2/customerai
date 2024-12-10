package com.sk.customer.messaging.sqslisteners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.Headers;

import java.util.Map;

public interface ListenerService {

     /**
      * Extract the target object from the String payload received. With this we can receive two different payload formats, on from a
      * direct App1->SQS-App2 and one with App1->SNS->SQS->App2 format. This require different handling.
      *
      * @param objectMapper      A Jackson ObjectMapper instance to handle the mashaling of the String.
      * @param source            The string payload
      * @param targetObjectClass The target class to return
      * @param <T>               Generics declaration of the response object
      * @return The object to be returned
      * @throws JsonProcessingException
      */
     default <T> T extractPayload(final ObjectMapper objectMapper, final String source, final Class<T> targetObjectClass) throws JsonProcessingException {
          var jsonNode = objectMapper.readTree(source);
          var topicArn = jsonNode.get("TopicArn");

          if (topicArn != null) {   // SNS based message - payload in the message node
               var sqsPayloadNode = jsonNode.get("Message");
               return objectMapper.readValue(sqsPayloadNode.textValue(), targetObjectClass);
          } else {
               return objectMapper.readValue(source, targetObjectClass);
          }
     }

     void receiveMessage(String masterPayload,
                         @Headers Map<String, Object> allHeaders);

}

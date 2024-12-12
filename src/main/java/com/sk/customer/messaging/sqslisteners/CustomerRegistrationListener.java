package com.sk.customer.messaging.sqslisteners;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.customer.service.VectorStoreService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import za.co.mamamoney.customer.dto.CustomerUpdatedDTO;
import za.co.mamamoney.customer.dto.SQSListenerDTO;
import za.co.mamamoney.customer.enums.CustomerManagementEventType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerRegistrationListener implements ListenerService {

     private final ObjectMapper objectMapper;
     private final VectorStoreService vectorStoreService;
     private final CustomerParagraphBuilder customerParagraphBuilder;

     @Override
     @SqsListener(value = "${cloud.aws.sqs.customer-rag-queue-name}")
     public void receiveMessage(String masterPayload, Map<String, Object> allHeaders) {
          try {

               var sqsListenerDTO = extractPayload(objectMapper, masterPayload, SQSListenerDTO.class);

               var eventType = sqsListenerDTO.getCustomerManagementEventType();

               switch (eventType) {
                    case CustomerManagementEventType.CUSTOMER_CREATION -> {
                         log.info("Received new customer registration event");
                         JsonNode node = objectMapper.valueToTree(sqsListenerDTO.getPayload());

                         var resource = convertJsonNodeToResource(node);
                         vectorStoreService.addCustomerData(resource);
                    }

                    case CustomerManagementEventType.CUSTOMER_UPDATE -> {
                         log.info("Received customer update event");

                         CustomerUpdatedDTO customerUpdatedDTO = objectMapper.convertValue(sqsListenerDTO.getPayload(), CustomerUpdatedDTO.class);

                         var paragraph = customerParagraphBuilder.generateCustomerParagraph(customerUpdatedDTO);

                         Map<String, Object> metadata =
                                 Map.of("mm-global-customer-id", customerUpdatedDTO.getMmGlobalCustomerId(),
                                         "msisdn", customerUpdatedDTO.getCustomerContactNumberDTOList().get(0).getContactNumber());
                         List<Document> documentList = List.of(new Document(paragraph, metadata));

                         vectorStoreService.persist(documentList);

//                         JsonNode node = objectMapper.valueToTree(sqsListenerDTO.getPayload());
//                         var msisdn = node.get("customer-contact-numbers").get(0).get("contact-number");
//
//                         var resource = convertJsonNodeToResource(node);
//                         vectorStoreService.addCustomerData(resource);
                    }

                    case CustomerManagementEventType.CUSTOMER_DEPRECATION -> {
                         log.info("Received customer deletion event");
                    }
                    case CustomerManagementEventType.CUSTOMER_REINSTATE -> {
                         log.info("Received customer reinstatement event");
                    }
               }
               log.debug("Received message [EventType : {}; messageSequenceId : {}]", sqsListenerDTO.getCustomerManagementEventType(), sqsListenerDTO.getMessageSequenceId());


               // vectorStoreService.loadJsonIntoVectorStore(node);

          } catch (Exception e) {
               log.error("Failed to process new customer request [payload={}]", masterPayload, e);
               throw new RuntimeException("Failed to process the sqs payload. " + e.getMessage());
          }
     }

     //   qa-feature-cs-rag-queue
     public Resource convertJsonNodeToResource(JsonNode jsonNode) {
          if (jsonNode == null) {
               throw new IllegalArgumentException("JsonNode cannot be null");
          }

          try {
               // Prefer file path if available
               if (jsonNode.has("filePath")) {
                    String filePath = jsonNode.get("filePath").asText();
                    return new ClassPathResource(filePath);
               }

               // Fallback to content conversion
               if (jsonNode.has("content")) {
                    String fileContent = jsonNode.get("content").asText();
                    return new ByteArrayResource(fileContent.getBytes(StandardCharsets.UTF_8));
               }

               // If no specific path or content, convert entire JsonNode
               ObjectMapper mapper = new ObjectMapper();
               byte[] jsonBytes = mapper.writeValueAsBytes(jsonNode);
               return new ByteArrayResource(jsonBytes);
          } catch (Exception e) {
               throw new RuntimeException("Failed to convert JsonNode to Resource", e);
          }
     }

}

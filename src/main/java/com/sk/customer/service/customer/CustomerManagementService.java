package com.sk.customer.service.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Service
@Slf4j
public class CustomerManagementService implements Function<JsonNode, String> {

     private final RestClient restClient;
     private final ObjectMapper objectMapper;

     public CustomerManagementService(RestClient.Builder builder, ObjectMapper objectMapper) {
          this.restClient = builder
                  .baseUrl("http://internal-customer-api-qa-feature.eng.mamamoney.co.za/api/v1/customer")
                  .build();
          this.objectMapper = objectMapper;
     }

     @Override
     public String apply(JsonNode msisdnPayload) {

          String msisdn = msisdnPayload.get("msisdn").toString().replace("\"","");

          log.info("Doing customer lookup using [msisdn : {}]", msisdn);
          var jsonNode = restClient.get()
                  .uri(uri->uri.queryParam("msisdn", msisdn)
                          .queryParam("loadRelations", "true")
                          .build())
                  .accept(MediaType.APPLICATION_JSON)
                  .retrieve()
                  .body(JsonNode.class);

          //String res = jsonNode.get("msisdn").toString();
          String res;
          try{
               res =objectMapper.writeValueAsString(jsonNode);
          } catch (JsonProcessingException e) {
               throw new RuntimeException(e);
          }

          return res;
     }

     public JsonNode retrieveCustomerByMsisdn(String msisdn) {
          return restClient.get()
                  .uri(uri->uri.queryParam("msisdn", msisdn).build())
                  .accept(MediaType.APPLICATION_JSON)
                  .retrieve()
                  .body(JsonNode.class);
     }
}

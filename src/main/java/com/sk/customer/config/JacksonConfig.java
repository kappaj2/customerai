package com.sk.customer.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

     @Bean(name = "objectMapper")
     @Qualifier("objectMapper")
     @Primary
     public ObjectMapper objectMapper() {
          var javaTimeModule = new JavaTimeModule();

          var objectMapper = new ObjectMapper();
          objectMapper = objectMapper.registerModule(javaTimeModule);

          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                  .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                  .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                  .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                  .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                  .getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);

          objectMapper = objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
          objectMapper = objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
          objectMapper = objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

          return objectMapper;
     }
}

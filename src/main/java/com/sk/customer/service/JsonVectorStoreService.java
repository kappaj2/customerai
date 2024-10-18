package com.sk.customer.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonMetadataGenerator;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JsonVectorStoreService {

     private static final int NUMBER_OF_TOKEN_PER_VECTOR = 350;
     private final VectorStore vectorStore;

     public void loadJsonIntoVectorStore(JsonNode jsonNode) {
          List<Document> documents = chunkJsonNode(jsonNode);
          vectorStore.add(documents);
     }

     private List<Document> chunkJsonNode(JsonNode jsonNode) {
          List<Document> documents = new ArrayList<>();

          // Assuming the JSON is an array of objects
          if (jsonNode.isArray()) {
               for (JsonNode node : jsonNode) {
                    documents.addAll(processJsonObject(node));
               }
          } else if (jsonNode.isObject()) {
               documents.addAll(processJsonObject(jsonNode));
          }

          return documents;
     }

     private List<Document> processJsonObject(JsonNode jsonObject) {
          List<Document> documents = new ArrayList<>();

          // Iterate through fields of the JSON object
          Iterator<String> fieldNames = jsonObject.fieldNames();
          while (fieldNames.hasNext()) {
               String fieldName = fieldNames.next();
               JsonNode fieldValue = jsonObject.get(fieldName);

               // Create a Document for each field
               String content = fieldValue.asText();
               Document document = new Document(content, Map.of("field", fieldName));

               documents.add(document);
          }

          return documents;
     }

     /*
      * Need to unpack this further. Looks like if we don't specify the keys to use, then it will use the whole doc - sort of what we want.
      * https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html
      */
     public void addCustomerData(Resource jsonResource) throws FileNotFoundException {

          //JsonReader jsonReader = new JsonReader(jsonResource, new CustomerMetadataGenerator(), "customer-id", "msisdn", "first-name", "last-name");

           JsonReader jsonReader = new JsonReader(jsonResource, new CustomerMetadataGenerator());
          var documentList = jsonReader.get();
          vectorStore.add(documentList);
     }

     public void addTextData(Resource textResource) throws FileNotFoundException {
          TextReader textReader = new TextReader(textResource);
          textReader.getCustomMetadata().put("filename", textResource.getFilename());
          List<Document> documentList = textReader.get();
          TextSplitter textSplitter = buildTokenTextSplitter(NUMBER_OF_TOKEN_PER_VECTOR);

          List<Document> splitDocuments = textSplitter.apply(documentList);

          vectorStore.add(splitDocuments);
     }

     public List<Document> queryJSONVector(String query) {
          List<Document> results = vectorStore.similaritySearch(
                  SearchRequest.defaults()
                          .withQuery(query)
                          .withTopK(2)
          );
          return results;
     }

     private TextSplitter buildTokenTextSplitter(int numberOfTokenPerVector) {
          return new TokenTextSplitter(numberOfTokenPerVector, 350, 5, 10000, true);
     }

     /**
      * This class is used to generate metadata for the customer data. The meta data are the data fields that will be returned.
      * This is used inside the JsonReader.
      */
     public class CustomerMetadataGenerator implements JsonMetadataGenerator {
          @Override
          public Map<String, Object> generate(Map<String, Object> jsonMap) {
               Map<String, Object> objectMap = Map.of("msisdn", jsonMap.get("msisdn"),
                       "first-name", jsonMap.get("first-name"),
                       "last-name", jsonMap.get("last-name"));
               return objectMap;
          }
     }
}

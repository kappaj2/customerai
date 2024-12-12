package com.sk.customer.messaging.sqslisteners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import za.co.mamamoney.customer.dto.CustomerUpdatedDTO;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CustomerParagraphBuilder {

     public String generateCustomerParagraph(CustomerUpdatedDTO customer) {
          // Create a map of placeholders
          Map<String, String> placeholders = new HashMap<>();

          // Add basic customer details
          placeholders.put("customerId", String.valueOf(customer.getCsCustomerId()));
          placeholders.put("gender", customer.getGender().toLowerCase());
          placeholders.put("firstName", customer.getFirstName());
          placeholders.put("lastName", customer.getLastName());
          placeholders.put("dateOfBirth", customer.getDateOfBirth().toString());
          placeholders.put("preferredLanguage", customer.getPreferredLanguage());
          placeholders.put("status", customer.getStatus().toLowerCase());
          placeholders.put("activationDate", customer.getActivationDate().toString());
          placeholders.put("inboundChannel", customer.getInboundChannel());
          placeholders.put("product", customer.getProduct());
          placeholders.put("brand", customer.getBrand());

          // Process optional details
          String paragraph = CUSTOMER_PARAGRAPH_TEMPLATE;

          // Add address details if available
          if (customer.getCustomerAddressDTOList() != null && !customer.getCustomerAddressDTOList().isEmpty()) {
               var address = customer.getCustomerAddressDTOList().get(0);
               placeholders.put("addressLine1", address.getAddressLine_1());
               placeholders.put("suburb", address.getSuburb());
               placeholders.put("city", address.getCity());
               placeholders.put("province", address.getProvince().name());
               placeholders.put("postalCode", address.getPostalCode());
               paragraph = paragraph.replace("{addressDetails}",ADDRESS_TEMPLATE);
          } else {
               paragraph = paragraph.replace("{addressDetails}", "");
          }

          // Add contact details if available
          if (customer.getCustomerContactNumberDTOList() != null && !customer.getCustomerContactNumberDTOList().isEmpty()) {
               var contact = customer.getCustomerContactNumberDTOList().get(0);
               placeholders.put("contactNumber", contact.getContactNumber());
               paragraph = paragraph.replace("{contactDetails}",CONTACT_TEMPLATE);
          } else {
               paragraph = paragraph.replace("{contactDetails}", "");
          }

          // Add identification details if available
          if (customer.getCustomerIdentificationNumberDTOList() != null && !customer.getCustomerIdentificationNumberDTOList().isEmpty()) {
               var idNumber = customer.getCustomerIdentificationNumberDTOList().get(0);
               placeholders.put("identificationNumber", idNumber.getIdentificationNumber());
               placeholders.put("identificationNumberType", idNumber.getIdentificationNumberTypeCode().name());
               placeholders.put("countryCode", idNumber.getIdentificationNumberCountryCode());
               paragraph = paragraph.replace("{identificationDetails}",IDENTIFICATION_TEMPLATE);
          } else {
               paragraph = paragraph.replace("{identificationDetails}", "");
          }

          // Replace all placeholders
          for (Map.Entry<String, String> entry : placeholders.entrySet()) {
               paragraph = paragraph.replace("{" + entry.getKey() + "}", entry.getValue());
          }

          log.debug("Build document with string : [doc: {}]", paragraph);
          return paragraph;
     }

     // Define a template with placeholders
     private static final String CUSTOMER_PARAGRAPH_TEMPLATE = """
             Customer {customerId} is a {gender} customer named {firstName} {lastName}, 
                                  "born on {dateOfBirth}. 
                                  "The customer's preferred language is {preferredLanguage} 
                                  "and their current status is {status}. 
                                  "They were activated on {activationDate}
                                  "through the {inboundChannel} channel 
                                  "and are subscribed to the {product} product 
                                  "under the {brand} brand.
                                  Address details as follows: {addressDetails}
                                  and Contact details as follows: {contactDetails}
                                  and identification details as follows: {identificationDetails}
             """;

     // Additional templates for optional sections
     private static final String ADDRESS_TEMPLATE = """
             Their residential address is located at {addressLine1}, 
                                  {suburb}, {city}, {province} {postalCode}.
             """;

     private static final String CONTACT_TEMPLATE = """
             Their primary contact number is {contactNumber}.
             """;

     private static final String IDENTIFICATION_TEMPLATE = """
             The customer's identification number is {identificationNumber}
                                  ({identificationNumberType}). Country of origin: {countryCode}.
             """;
}

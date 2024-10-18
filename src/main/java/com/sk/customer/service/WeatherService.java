package com.sk.customer.service;

import com.sk.customer.dto.WeatherConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Slf4j
public class WeatherService implements Function<WeatherService.Request, WeatherService.Response> {

     private final RestClient restClient;
     private final WeatherConfigProperties weatherProps;

     public WeatherService(WeatherConfigProperties props) {
          this.weatherProps = props;
          log.info("Weather API URL: {}", weatherProps.apiUrl());
          log.info("Weather API Key: {}", weatherProps.apiKey());
          this.restClient = RestClient.create(weatherProps.apiUrl());
     }

     @Override
     public Response apply(Request weatherRequest) {
          log.info("Weather Request: {}", weatherRequest);
          Response response = restClient.get()
                  .uri("/current.json?key={key}&q={q}", weatherProps.apiKey(), weatherRequest.city())
                  .retrieve()
                  .body(Response.class);
          log.info("Weather API Response: {}", response);
          return response;
     }

     // mapping the response of the Weather API to records. I only mapped the information I was interested in.
     public record Request(String city) {
     }

     public record Response(Location location, Current current) {
     }

     public record Location(String name, String region, String country, Long lat, Long lon) {
     }

     public record Current(String temp_f, Condition condition, String wind_mph, String humidity) {
     }

     public record Condition(String text) {
     }
}

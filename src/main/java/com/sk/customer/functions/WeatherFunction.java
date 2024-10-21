package com.sk.customer.functions;

import com.sk.customer.dto.WeatherConfigProperties;
import com.sk.customer.service.WeatherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class WeatherFunction {

     private final WeatherConfigProperties props;

     public WeatherFunction(WeatherConfigProperties props) {
          this.props = props;
     }

     @Bean
     @Description("Get the current weather conditions for the given city.")
     public Function<WeatherService.Request,WeatherService.Response> currentWeatherFunction() {
          return new WeatherService(props);
     }
}

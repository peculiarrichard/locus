package com.locus.distraction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

// RestClient.Builder doesn't autoconfigure as an injectable bean on this module, the same gap
// found with WebClient.Builder on the Gateway module in this Spring Boot 4.1.1 release train.
@Configuration
public class RestClientConfig {

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}

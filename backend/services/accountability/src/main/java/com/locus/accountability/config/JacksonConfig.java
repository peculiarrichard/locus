package com.locus.accountability.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ObjectMapper doesn't autoconfigure as an injectable bean on this module, the same gap found on
// the Gateway/Session/Distraction/Goal/Analytics modules in this Spring Boot 4.1.1 release train.
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }
}

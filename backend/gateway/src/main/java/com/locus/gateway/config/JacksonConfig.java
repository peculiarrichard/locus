package com.locus.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Provides the ObjectMapper bean directly — Spring Boot's own Jackson autoconfiguration doesn't
// activate on this module (a Spring Boot 4 modularization gap found while implementing, matching
// the same pattern as Flyway's and TestRestTemplate's own starter-not-just-library requirement).
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}

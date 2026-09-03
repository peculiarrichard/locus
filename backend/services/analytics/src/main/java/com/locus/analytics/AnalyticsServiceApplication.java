package com.locus.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for the Analytics Service — computes study habit metrics from session and distraction events.
@SpringBootApplication
public class AnalyticsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsServiceApplication.class, args);
  }
}

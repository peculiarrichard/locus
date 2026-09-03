package com.locus.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Entry point for the Analytics Service — computes study habit metrics from session and distraction events.
@SpringBootApplication
@EnableScheduling
public class AnalyticsServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnalyticsServiceApplication.class, args);
  }
}

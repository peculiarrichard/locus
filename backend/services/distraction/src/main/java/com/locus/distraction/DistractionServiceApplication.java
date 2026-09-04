package com.locus.distraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Entry point for the Distraction Logging Service — records blur/focus distraction events.
@SpringBootApplication
@EnableScheduling
public class DistractionServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DistractionServiceApplication.class, args);
  }
}

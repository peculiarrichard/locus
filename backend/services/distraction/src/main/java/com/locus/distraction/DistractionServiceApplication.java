package com.locus.distraction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for the Distraction Logging Service — records blur/focus distraction events.
@SpringBootApplication
public class DistractionServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DistractionServiceApplication.class, args);
  }
}

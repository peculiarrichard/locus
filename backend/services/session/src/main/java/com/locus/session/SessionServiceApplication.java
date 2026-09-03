package com.locus.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for the Session Service — starts, tracks, and completes focus sessions.
@SpringBootApplication
public class SessionServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SessionServiceApplication.class, args);
  }
}

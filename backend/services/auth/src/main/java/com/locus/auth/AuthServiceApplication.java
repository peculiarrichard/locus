package com.locus.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Entry point for the Auth & User Service — registration, login, MFA, tokens, and admin operations.
@SpringBootApplication
@EnableScheduling
public class AuthServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuthServiceApplication.class, args);
  }
}

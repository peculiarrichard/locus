package com.locus.goal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Entry point for the Goal and Plan Service — manages study goals, milestones, and deadlines.
@SpringBootApplication
public class GoalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoalServiceApplication.class, args);
    }
}

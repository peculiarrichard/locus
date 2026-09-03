package com.locus.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

// SMTP locally (Mailpit, per infra/local/docker-compose.yml) — production swaps this for SES via
// IRSA (technical-spec.md §6), a Part 2 concern; this abstraction is what lets that swap happen
// without touching any calling code.
@Component
public class EmailSender {

  private final JavaMailSender javaMailSender;
  private final String fromAddress;

  public EmailSender(JavaMailSender javaMailSender, @Value("${locus.notification.from-address}") String fromAddress) {
    this.javaMailSender = javaMailSender;
    this.fromAddress = fromAddress;
  }

  public void send(String toAddress, String subject, String body) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(toAddress);
    message.setSubject(subject);
    message.setText(body);
    javaMailSender.send(message);
  }
}

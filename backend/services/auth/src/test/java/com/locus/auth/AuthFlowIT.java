package com.locus.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// End-to-end integration test for registration/login validation and the public JWKS endpoint,
// against real Postgres and LocalStack containers. Uses plain RestClient rather than
// spring-boot-resttestclient, which has a known runtime-classpath bug on Spring Boot 4.1.1.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthFlowIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

  // Requires LOCALSTACK_AUTH_TOKEN in the environment (see prerequisites.md) —
  // LocalStack has
  // required a free account token for any use, including CI, since March 2026.
  @Container
  static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
      .withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS)
      .withEnv("LOCALSTACK_AUTH_TOKEN", System.getenv("LOCALSTACK_AUTH_TOKEN"));

  @DynamicPropertySource
  static void configure(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.cloud.aws.sns.endpoint",
        () -> localstack.getEndpointOverride(LocalStackContainer.Service.SNS).toString());
    registry.add("spring.cloud.aws.credentials.access-key", () -> "test");
    registry.add("spring.cloud.aws.credentials.secret-key", () -> "test");
    registry.add("spring.cloud.aws.region.static", localstack::getRegion);
  }

  @LocalServerPort
  private int port;
  private RestClient client;

  @BeforeEach
  void setUp() {
    client = RestClient.create("http://localhost:" + port);
  }

  private HttpStatus register(String email, String password) {
    try {
      return (HttpStatus) client.post().uri("/auth/register").body(Map.of("email", email, "password", password))
          .retrieve().toBodilessEntity().getStatusCode();
    } catch (HttpClientErrorException e) {
      return (HttpStatus) e.getStatusCode();
    }
  }

  private HttpStatus login(String email, String password) {
    try {
      return (HttpStatus) client.post().uri("/auth/login").body(Map.of("email", email, "password", password)).retrieve()
          .toBodilessEntity().getStatusCode();
    } catch (HttpClientErrorException e) {
      return (HttpStatus) e.getStatusCode();
    }
  }

  @Test
  void registerRejectsWeakPassword() {
    assertThat(register("weak@example.com", "short")).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void registerAcceptsAValidAccount() {
    assertThat(register("flow@example.com", "Testpass1!")).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void loginRejectsUnverifiedAccount() {
    register("unverified@example.com", "Testpass1!");
    assertThat(login("unverified@example.com", "Testpass1!")).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void loginRejectsWrongPassword() {
    register("wrongpw@example.com", "Testpass1!");
    assertThat(login("wrongpw@example.com", "Nope1234!")).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void jwksEndpointIsPublicAndReturnsAKey() {
    String body = client.get().uri("/.well-known/jwks.json").retrieve().body(String.class);
    assertThat(body).contains("\"kty\":\"RSA\"");
  }
}

package com.locus.gateway.config;

import com.locus.gateway.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

// Fail-fast JWT verification per technical-spec.md §1's zero-trust model — a convenience layer,
// not the sole authority; every backend service independently re-validates the same token. The
// original bearer token is forwarded unchanged downstream, never replaced with a trusted header.
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private static final String[] PUBLIC_PATHS = {"/api/v1/auth/register", "/api/v1/auth/login",
      "/api/v1/auth/mfa/challenge", "/api/v1/auth/refresh", "/api/v1/auth/verify-email",
      "/api/v1/auth/verify-email/resend", "/api/v1/auth/password-reset/**", "/actuator/health/**", "/gateway/fallback"};

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
      JwtAuthenticationEntryPoint entryPoint) {
    http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(SecurityConfig::allowPublicPathsOnly)
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {
        }).authenticationEntryPoint(entryPoint));
    return http.build();
  }

  private static void allowPublicPathsOnly(AuthorizeExchangeSpec exchange) {
    // A CORS preflight is always an OPTIONS request carrying no Authorization header — without this,
    // every authenticated route's preflight gets a 401 from this filter chain before globalcors'
    // CorsWebFilter ever gets a chance to answer it, breaking the real request that would follow.
    exchange.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll().pathMatchers(PUBLIC_PATHS).permitAll()
        .anyExchange().authenticated();
  }
}

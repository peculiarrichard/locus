package com.locus.auth.config;

import com.locus.auth.security.JwtKeyProvider;
import com.nimbusds.jose.JOSEException;
import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

// Wires request-time JWT verification, role mapping, and the public-route allowlist per frd.md's Auth Service section.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtKeyProvider jwtKeyProvider) throws JOSEException {
    return NimbusJwtDecoder.withPublicKey(jwtKeyProvider.getRsaJwk().toRSAPublicKey()).build();
  }

  // The "roles" claim holds plain names ("admin"); hasRole("ADMIN") expects a
  // ROLE_ADMIN authority, so map and uppercase here rather than relying on the
  // default scope-based converter.
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::rolesToAuthorities);
    return converter;
  }

  private static Collection<GrantedAuthority> rolesToAuthorities(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    if (roles == null) {
      return List.of();
    }
    return roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        .toList();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/auth/register", "/auth/login", "/auth/mfa/challenge", "/auth/refresh",
                "/auth/verify-email", "/auth/verify-email/resend", "/auth/password-reset/**", "/.well-known/jwks.json",
                "/actuator/health/**")
            .permitAll().requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }
}

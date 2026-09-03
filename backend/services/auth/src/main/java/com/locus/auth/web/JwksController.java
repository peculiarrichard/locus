package com.locus.auth.web;

import com.locus.auth.security.JwtKeyProvider;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Exposes this service's public signing key set so every other service can independently verify issued JWTs.
@RestController
public class JwksController {

  private final JwtKeyProvider jwtKeyProvider;

  public JwksController(JwtKeyProvider jwtKeyProvider) {
    this.jwtKeyProvider = jwtKeyProvider;
  }

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> jwks() {
    return new JWKSet(jwtKeyProvider.getRsaJwk().toPublicJWK()).toJSONObject();
  }
}

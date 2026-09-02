package com.locus.auth.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import org.springframework.stereotype.Component;

// Holds the RS256 signing keypair for this instance, generated fresh at startup for local dev;
// Part 2 swaps this for a Secrets-Manager-backed persistent key.
@Component
public class JwtKeyProvider {

  private final RSAKey rsaJwk;

  public JwtKeyProvider() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair keyPair = generator.generateKeyPair();
      this.rsaJwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
          .privateKey((RSAPrivateKey) keyPair.getPrivate()).keyID(UUID.randomUUID().toString())
          .algorithm(JWSAlgorithm.RS256).build();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to generate RSA keypair", e);
    }
  }

  public RSAKey getRsaJwk() {
    return rsaJwk;
  }

  public String getKeyId() {
    return rsaJwk.getKeyID();
  }
}

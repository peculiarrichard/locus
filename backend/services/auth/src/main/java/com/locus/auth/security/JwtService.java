package com.locus.auth.security;

import com.locus.auth.domain.Role;
import com.locus.auth.domain.User;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

// Issues and verifies the RS256 JWTs this service is the sole minter of, per technical-spec.md's zero-trust model.
@Component
public class JwtService {

  public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
  public static final Duration MFA_CHALLENGE_TTL = Duration.ofMinutes(5);

  private final JwtKeyProvider jwtKeyProvider;

  public JwtService(JwtKeyProvider jwtKeyProvider) {
    this.jwtKeyProvider = jwtKeyProvider;
  }

  public String issueAccessToken(User user, boolean mfaCompleted) {
    List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(user.getId().toString()).claim("roles", roles)
        .claim("mfa", mfaCompleted).issueTime(Date.from(now)).expirationTime(Date.from(now.plus(ACCESS_TOKEN_TTL)))
        .jwtID(UUID.randomUUID().toString()).build();
    return sign(claims);
  }

  public String issueMfaChallengeToken(UUID userId) {
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(userId.toString()).claim("purpose", "mfa_challenge")
        .issueTime(Date.from(now)).expirationTime(Date.from(now.plus(MFA_CHALLENGE_TTL)))
        .jwtID(UUID.randomUUID().toString()).build();
    return sign(claims);
  }

  public UUID verifyMfaChallengeToken(String token) {
    try {
      SignedJWT signedJwt = SignedJWT.parse(token);
      RSASSAVerifier verifier = new RSASSAVerifier(jwtKeyProvider.getRsaJwk().toRSAPublicKey());
      if (!signedJwt.verify(verifier)) {
        throw new IllegalArgumentException("Invalid MFA challenge token signature");
      }
      JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
      if (!"mfa_challenge".equals(claims.getStringClaim("purpose"))) {
        throw new IllegalArgumentException("Token is not an MFA challenge token");
      }
      if (claims.getExpirationTime().before(new Date())) {
        throw new IllegalArgumentException("MFA challenge token expired");
      }
      return UUID.fromString(claims.getSubject());
    } catch (ParseException | JOSEException e) {
      throw new IllegalArgumentException("Malformed MFA challenge token", e);
    }
  }

  private String sign(JWTClaimsSet claims) {
    try {
      SignedJWT signedJwt = new SignedJWT(
          new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(jwtKeyProvider.getKeyId()).build(), claims);
      signedJwt.sign(new RSASSASigner(jwtKeyProvider.getRsaJwk()));
      return signedJwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Unable to sign JWT", e);
    }
  }
}

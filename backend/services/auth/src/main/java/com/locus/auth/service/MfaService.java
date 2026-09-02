package com.locus.auth.service;

import com.locus.auth.domain.MfaRecoveryCode;
import com.locus.auth.domain.MfaSecret;
import com.locus.auth.domain.User;
import com.locus.auth.exception.ApiException;
import com.locus.auth.repository.MfaRecoveryCodeRepository;
import com.locus.auth.repository.MfaSecretRepository;
import com.locus.auth.repository.UserRepository;
import com.locus.auth.security.MfaSecretCipher;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TOTP MFA enrollment, confirmation, challenge verification, and disable, per frd.md's Auth & User Service section.
@Service
public class MfaService {

  private static final int RECOVERY_CODE_COUNT = 10;

  private final UserRepository userRepository;
  private final MfaSecretRepository mfaSecretRepository;
  private final MfaRecoveryCodeRepository recoveryCodeRepository;
  private final MfaSecretCipher secretCipher;
  private final PasswordEncoder passwordEncoder;
  private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
  private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(),
      new SystemTimeProvider());
  private final SecureRandom secureRandom = new SecureRandom();

  public MfaService(UserRepository userRepository, MfaSecretRepository mfaSecretRepository,
      MfaRecoveryCodeRepository recoveryCodeRepository, MfaSecretCipher secretCipher, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.mfaSecretRepository = mfaSecretRepository;
    this.recoveryCodeRepository = recoveryCodeRepository;
    this.secretCipher = secretCipher;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public String enroll(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    mfaSecretRepository.findById(userId).ifPresent(existing -> {
      if (existing.isConfirmed()) {
        throw ApiException.conflict("MFA_ALREADY_ENABLED", "MFA is already enabled");
      }
      // Re-enrolling before confirming replaces the old, unconfirmed secret rather
      // than accumulating orphans.
      mfaSecretRepository.delete(existing);
    });
    String secret = secretGenerator.generate();
    mfaSecretRepository.save(new MfaSecret(userId, secretCipher.encrypt(secret)));
    return buildOtpAuthUri(user.getEmail(), secret);
  }

  @Transactional
  public List<String> confirm(UUID userId, String code) {
    MfaSecret mfaSecret = mfaSecretRepository.findById(userId)
        .orElseThrow(() -> ApiException.notFound("MFA enrollment"));
    if (mfaSecret.isConfirmed()) {
      throw ApiException.conflict("MFA_ALREADY_ENABLED", "MFA is already enabled");
    }
    String secret = secretCipher.decrypt(mfaSecret.getEncryptedSecret());
    if (!codeVerifier.isValidCode(secret, code)) {
      throw ApiException.invalidToken("MFA code");
    }
    mfaSecret.setConfirmed(true);
    mfaSecret.setEnabledAt(Instant.now());
    mfaSecretRepository.save(mfaSecret);
    return generateRecoveryCodes(userId);
  }

  @Transactional
  public void disable(UUID userId, String password) {
    User user = userRepository.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw ApiException.invalidCredentials();
    }
    mfaSecretRepository.deleteById(userId);
  }

  @Transactional
  public boolean verifyCodeOrRecoveryCode(UUID userId, String code) {
    MfaSecret mfaSecret = mfaSecretRepository.findById(userId).orElse(null);
    if (mfaSecret == null || !mfaSecret.isConfirmed()) {
      return false;
    }
    String secret = secretCipher.decrypt(mfaSecret.getEncryptedSecret());
    if (codeVerifier.isValidCode(secret, code)) {
      return true;
    }
    return consumeRecoveryCodeIfValid(userId, code);
  }

  private boolean consumeRecoveryCodeIfValid(UUID userId, String code) {
    for (MfaRecoveryCode recoveryCode : recoveryCodeRepository.findByUserIdAndUsedAtIsNull(userId)) {
      if (passwordEncoder.matches(code, recoveryCode.getCodeHash())) {
        recoveryCode.setUsedAt(Instant.now());
        recoveryCodeRepository.save(recoveryCode);
        return true;
      }
    }
    return false;
  }

  private List<String> generateRecoveryCodes(UUID userId) {
    List<String> rawCodes = new ArrayList<>();
    for (int i = 0; i < RECOVERY_CODE_COUNT; i++) {
      String code = String.format("%08d", secureRandom.nextInt(100_000_000));
      rawCodes.add(code);
      recoveryCodeRepository.save(new MfaRecoveryCode(userId, passwordEncoder.encode(code)));
    }
    return rawCodes;
  }

  private String buildOtpAuthUri(String email, String secret) {
    QrData data = new QrData.Builder().label(email).secret(secret).issuer("Locus").algorithm(HashingAlgorithm.SHA1)
        .digits(6).period(30).build();
    return data.getUri();
  }
}

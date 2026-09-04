package com.locus.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.locus.notification.domain.PendingPartnerActivity;
import com.locus.notification.domain.UserContact;
import com.locus.notification.repository.LastSessionActivityRepository;
import com.locus.notification.repository.NotificationLogRepository;
import com.locus.notification.repository.PendingPartnerActivityRepository;
import com.locus.notification.repository.UserContactRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationServiceTest {

  private UserContactRepository userContactRepository;
  private LastSessionActivityRepository lastSessionActivityRepository;
  private NotificationLogRepository notificationLogRepository;
  private PendingPartnerActivityRepository pendingPartnerActivityRepository;
  private EmailSender emailSender;
  private NotificationService service;
  private final UUID userId = UUID.randomUUID();
  private final UUID partnerId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    userContactRepository = mock(UserContactRepository.class);
    lastSessionActivityRepository = mock(LastSessionActivityRepository.class);
    notificationLogRepository = mock(NotificationLogRepository.class);
    pendingPartnerActivityRepository = mock(PendingPartnerActivityRepository.class);
    emailSender = mock(EmailSender.class);
    service = new NotificationService(
        userContactRepository,
        lastSessionActivityRepository,
        notificationLogRepository,
        pendingPartnerActivityRepository,
        emailSender,
        "http://localhost:8081/auth/verify-email?token=",
        "http://localhost:8081/auth/password-reset/confirm?token=");
    when(userContactRepository.save(any(UserContact.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void userRegisteredCreatesContactAndSendsVerificationEmail() {
    when(userContactRepository.findById(userId)).thenReturn(Optional.empty());

    service.onUserRegistered(userId, "a@example.com", "Alex", "tok123");

    verify(emailSender).send(org.mockito.ArgumentMatchers.eq("a@example.com"), anyString(),
        org.mockito.ArgumentMatchers.contains("tok123"));
  }

  @Test
  void noEmailSentForABouncedContact() {
    UserContact contact = contactWith(false);
    ReflectionTestUtils.setField(contact, "bounced", true);
    when(userContactRepository.findById(userId)).thenReturn(Optional.of(contact));

    service.onStreakBroken(userId, 5);

    verify(emailSender, never()).send(anyString(), anyString(), anyString());
  }

  @Test
  void missingContactIsSkippedSilentlyNotThrown() {
    when(userContactRepository.findById(userId)).thenReturn(Optional.empty());

    service.onStreakBroken(userId, 5);

    verify(emailSender, never()).send(anyString(), anyString(), anyString());
  }

  @Test
  void sessionCompletedPartnerActivityIsBatchedNotSentImmediately() {
    service.onPartnerActivity(userId, partnerId, "session_completed", Instant.now());

    verify(pendingPartnerActivityRepository).save(any(PendingPartnerActivity.class));
    verify(emailSender, never()).send(anyString(), anyString(), anyString());
  }

  @Test
  void streakBrokenPartnerActivityIsSentImmediately() {
    when(userContactRepository.findById(userId)).thenReturn(Optional.of(contactWith(false)));

    service.onPartnerActivity(userId, partnerId, "streak_broken", Instant.now());

    verify(emailSender).send(anyString(), anyString(), anyString());
    verify(pendingPartnerActivityRepository, never()).save(any());
  }

  @Test
  void weeklySummaryIncludesBatchedPartnerActivityThenClearsIt() {
    when(userContactRepository.findById(userId)).thenReturn(Optional.of(contactWith(false)));
    when(pendingPartnerActivityRepository.findByUserId(userId))
        .thenReturn(List.of(new PendingPartnerActivity(userId, partnerId, "session_completed", Instant.now())));

    service.onWeeklySummaryDue(userId, LocalDate.now().minusDays(6), LocalDate.now(), List.of(9, 14), 0.8, 3);

    verify(emailSender).send(anyString(), anyString(), org.mockito.ArgumentMatchers.contains("1 session(s)"));
    verify(pendingPartnerActivityRepository).deleteByUserId(userId);
  }

  @Test
  void sesBounceMarksContactBounced() {
    UserContact contact = contactWith(false);
    when(userContactRepository.findByEmail("a@example.com")).thenReturn(Optional.of(contact));

    service.onSesBounce("a@example.com", "Permanent");

    assertThat(contact.isBounced()).isTrue();
  }

  @Test
  void purgeUserDeletesAllOwnedData() {
    service.purgeUser(userId);

    verify(userContactRepository).deleteById(userId);
    verify(lastSessionActivityRepository).deleteById(userId);
    verify(pendingPartnerActivityRepository).deleteByUserId(userId);
  }

  private UserContact contactWith(boolean bounced) {
    UserContact contact = new UserContact(userId, "a@example.com", "Alex", "Africa/Lagos");
    ReflectionTestUtils.setField(contact, "bounced", bounced);
    return contact;
  }
}

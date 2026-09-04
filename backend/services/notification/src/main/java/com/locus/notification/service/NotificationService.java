package com.locus.notification.service;

import com.locus.notification.domain.LastSessionActivity;
import com.locus.notification.domain.NotificationLog;
import com.locus.notification.domain.PendingPartnerActivity;
import com.locus.notification.domain.UserContact;
import com.locus.notification.exception.ApiException;
import com.locus.notification.repository.LastSessionActivityRepository;
import com.locus.notification.repository.NotificationLogRepository;
import com.locus.notification.repository.PendingPartnerActivityRepository;
import com.locus.notification.repository.UserContactRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Business logic for every consumed event plus the reminder scan, per frd.md's Notification
// Service section. Every send is best-effort against a possibly-missing UserContact row (a race
// with the UserRegistered event that creates it) rather than throwing — a notification is never
// on the critical path of the action that triggered it.
@Service
public class NotificationService {

  // session_completed activity is deliberately not sent immediately, per frd.md's anti-spam
  // decision — see handlePartnerActivity below.
  private static final List<String> IMMEDIATE_ACTIVITY_TYPES = List.of("streak_broken", "member_joined",
      "account_deleted");

  private final UserContactRepository userContactRepository;
  private final LastSessionActivityRepository lastSessionActivityRepository;
  private final NotificationLogRepository notificationLogRepository;
  private final PendingPartnerActivityRepository pendingPartnerActivityRepository;
  private final EmailSender emailSender;
  private final String verificationLinkBase;
  private final String passwordResetLinkBase;

  public NotificationService(
      UserContactRepository userContactRepository,
      LastSessionActivityRepository lastSessionActivityRepository,
      NotificationLogRepository notificationLogRepository,
      PendingPartnerActivityRepository pendingPartnerActivityRepository,
      EmailSender emailSender,
      @Value("${locus.notification.verification-link-base}") String verificationLinkBase,
      @Value("${locus.notification.password-reset-link-base}") String passwordResetLinkBase) {
    this.userContactRepository = userContactRepository;
    this.lastSessionActivityRepository = lastSessionActivityRepository;
    this.notificationLogRepository = notificationLogRepository;
    this.pendingPartnerActivityRepository = pendingPartnerActivityRepository;
    this.emailSender = emailSender;
    this.verificationLinkBase = verificationLinkBase;
    this.passwordResetLinkBase = passwordResetLinkBase;
  }

  @Transactional
  public void onUserRegistered(UUID userId, String email, String displayName, String verificationToken) {
    UserContact contact = userContactRepository.findById(userId)
        .orElseGet(() -> new UserContact(userId, email, displayName, "Africa/Lagos"));
    contact.setEmail(email);
    contact.setDisplayName(displayName);
    contact.touch();
    userContactRepository.save(contact);
    sendAndLog(
        userId,
        email,
        "verification",
        "Verify your Locus account",
        "Welcome to Locus. Verify your account: " + verificationLinkBase + verificationToken);
  }

  @Transactional
  public void onUserProfileUpdated(UUID userId, String email, String displayName, String timezone) {
    UserContact contact = userContactRepository.findById(userId)
        .orElseGet(() -> new UserContact(userId, email, displayName, timezone));
    contact.setEmail(email);
    contact.setDisplayName(displayName);
    contact.setTimezone(timezone);
    contact.touch();
    userContactRepository.save(contact);
  }

  @Transactional
  public void onPasswordResetRequested(UUID userId, String email, String resetToken) {
    sendAndLog(
        userId,
        email,
        "password_reset",
        "Reset your Locus password",
        "Reset your password: " + passwordResetLinkBase + resetToken);
  }

  @Transactional
  public void onGoalDeadlineApproaching(UUID userId, String goalType, int daysRemaining) {
    withContact(userId, contact -> sendAndLog(
        userId, contact.getEmail(), "goal_deadline",
        GoalDeadlineTone.subjectFor(daysRemaining), GoalDeadlineTone.bodyFor(daysRemaining, goalType)));
  }

  @Transactional
  public void onStreakBroken(UUID userId, int streakLengthBeforeBreak) {
    withContact(
        userId,
        contact -> sendAndLog(
            userId,
            contact.getEmail(),
            "streak_broken",
            "Your streak ended",
            "Your " + streakLengthBeforeBreak + "-day streak ended. Start a new session whenever you're ready."));
  }

  @Transactional
  public void onPartnerActivity(UUID userId, UUID partnerUserId, String activityType, Instant occurredAt) {
    if (!IMMEDIATE_ACTIVITY_TYPES.contains(activityType)) {
      pendingPartnerActivityRepository
          .save(new PendingPartnerActivity(userId, partnerUserId, activityType, occurredAt));
      return;
    }
    withContact(
        userId,
        contact -> sendAndLog(
            userId,
            contact.getEmail(),
            "partner_activity_" + activityType,
            "Activity from your accountability group",
            partnerActivityBody(activityType)));
  }

  @Transactional
  public void onSessionCompleted(UUID userId, Instant completedAt) {
    LastSessionActivity activity = lastSessionActivityRepository.findById(userId)
        .orElseGet(() -> new LastSessionActivity(userId, completedAt));
    activity.setLastCompletedAt(completedAt);
    lastSessionActivityRepository.save(activity);
  }

  @Transactional
  public void onWeeklySummaryDue(
      UUID userId, LocalDate weekStart, LocalDate weekEnd, List<Integer> bestHours, double completionRate,
      int distractionCount) {
    List<PendingPartnerActivity> batched = pendingPartnerActivityRepository.findByUserId(userId);
    withContact(
        userId,
        contact -> {
          StringBuilder body = new StringBuilder();
          body.append("Your week (")
              .append(weekStart)
              .append(" to ")
              .append(weekEnd)
              .append("): completion rate ")
              .append(Math.round(completionRate * 100))
              .append("%, ")
              .append(distractionCount)
              .append(" distractions logged.");
          if (!bestHours.isEmpty()) {
            body.append(" Best study hours: ").append(bestHours).append('.');
          }
          if (!batched.isEmpty()) {
            body.append(' ').append(batched.size())
                .append(" session(s) completed by your accountability group this week.");
          }
          sendAndLog(userId, contact.getEmail(), "weekly_summary", "Your weekly Locus summary", body.toString());
        });
    pendingPartnerActivityRepository.deleteByUserId(userId);
  }

  @Transactional
  public void onSesBounce(String email, String bounceType) {
    userContactRepository
        .findByEmail(email)
        .ifPresent(
            contact -> {
              contact.setBounced(true);
              userContactRepository.save(contact);
            });
  }

  @Transactional
  public void purgeUser(UUID userId) {
    userContactRepository.deleteById(userId);
    lastSessionActivityRepository.deleteById(userId);
    pendingPartnerActivityRepository.deleteByUserId(userId);
  }

  @Transactional(readOnly = true)
  public Optional<UserContact> getPreferences(UUID userId) {
    return userContactRepository.findById(userId);
  }

  @Transactional
  public UserContact updatePreferences(UUID userId, LocalTime reminderTime) {
    UserContact contact = userContactRepository
        .findById(userId)
        .orElseThrow(() -> ApiException.badRequest("CONTACT_NOT_FOUND", "No contact record yet for this account"));
    contact.setReminderTime(reminderTime);
    contact.touch();
    return userContactRepository.save(contact);
  }

  private String partnerActivityBody(String activityType) {
    return switch (activityType) {
      case "streak_broken" -> "A member of your accountability group had their streak break.";
      case "member_joined" -> "A new member joined your accountability group.";
      case "account_deleted" -> "A member of your accountability group deleted their account. That pairing has ended.";
      default -> "There's new activity in your accountability group.";
    };
  }

  private void withContact(UUID userId, Consumer<UserContact> action) {
    userContactRepository.findById(userId).filter(c -> !c.isBounced()).ifPresent(action);
  }

  private void sendAndLog(UUID userId, String email, String type, String subject, String body) {
    if (email == null) {
      return;
    }
    emailSender.send(email, subject, body);
    notificationLogRepository.save(new NotificationLog(userId, type));
  }
}

package com.locus.notification.service;

import com.locus.notification.domain.LastSessionActivity;
import com.locus.notification.domain.NotificationLog;
import com.locus.notification.domain.UserContact;
import com.locus.notification.repository.LastSessionActivityRepository;
import com.locus.notification.repository.NotificationLogRepository;
import com.locus.notification.repository.UserContactRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Per-user scheduled reminder job, per frd.md's reminder-scheduling resolution: at each user's
// own configured time, checks whether they've already studied today and nudges if not. Runs
// every minute rather than once a day, since "each user's configured time" isn't a single fixed
// wall-clock moment the way the other services' daily scans are.
@Component
public class ReminderScanJob {

  private final UserContactRepository userContactRepository;
  private final LastSessionActivityRepository lastSessionActivityRepository;
  private final NotificationLogRepository notificationLogRepository;
  private final EmailSender emailSender;

  public ReminderScanJob(
      UserContactRepository userContactRepository,
      LastSessionActivityRepository lastSessionActivityRepository,
      NotificationLogRepository notificationLogRepository,
      EmailSender emailSender) {
    this.userContactRepository = userContactRepository;
    this.lastSessionActivityRepository = lastSessionActivityRepository;
    this.notificationLogRepository = notificationLogRepository;
    this.emailSender = emailSender;
  }

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void scan() {
    for (UserContact contact : userContactRepository.findAll()) {
      if (contact.getReminderTime() == null || contact.isBounced()) {
        continue;
      }
      ZoneId zone = ZoneId.of(contact.getTimezone());
      ZonedDateTime nowLocal = ZonedDateTime.now(zone);
      LocalTime currentMinute = nowLocal.toLocalTime().withSecond(0).withNano(0);
      if (!currentMinute.equals(contact.getReminderTime().withSecond(0).withNano(0))) {
        continue;
      }
      LocalDate today = nowLocal.toLocalDate();
      if (today.equals(contact.getLastReminderSentDate())) {
        continue;
      }
      if (studiedToday(contact.getUserId(), zone, today)) {
        continue;
      }
      emailSender.send(
          contact.getEmail(),
          "A quick nudge from Locus",
          "You haven't logged a study session today yet — even a short one keeps things moving.");
      notificationLogRepository.save(new NotificationLog(contact.getUserId(), "reminder"));
      contact.setLastReminderSentDate(today);
      userContactRepository.save(contact);
    }
  }

  private boolean studiedToday(UUID userId, ZoneId zone, LocalDate today) {
    Optional<LastSessionActivity> activity = lastSessionActivityRepository.findById(userId);
    return activity.map(a -> a.getLastCompletedAt().atZone(zone).toLocalDate().equals(today)).orElse(false);
  }
}

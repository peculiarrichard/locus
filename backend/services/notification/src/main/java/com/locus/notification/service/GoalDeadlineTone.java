package com.locus.notification.service;

import java.util.Locale;

// Tone scales with urgency across the 30/14/7/1-day thresholds, per frd.md.
final class GoalDeadlineTone {

  private GoalDeadlineTone() {
  }

  static String subjectFor(int daysRemaining) {
    if (daysRemaining <= 1) {
      return "Your deadline is tomorrow";
    }
    if (daysRemaining <= 7) {
      return "One week until your deadline";
    }
    if (daysRemaining <= 14) {
      return "Two weeks until your deadline";
    }
    return "Your deadline is approaching";
  }

  static String bodyFor(int daysRemaining, String goalType) {
    String goal = "Your " + goalType.toLowerCase(Locale.ROOT) + " goal";
    if (daysRemaining <= 1) {
      return goal + " is due tomorrow. This is your last check-in before it arrives.";
    }
    if (daysRemaining <= 7) {
      return goal + " is due in " + daysRemaining + " days. Worth a final push this week.";
    }
    if (daysRemaining <= 14) {
      return goal + " is due in " + daysRemaining + " days. A good time to review your progress.";
    }
    return goal + " is due in " + daysRemaining + " days. Just a heads up.";
  }
}

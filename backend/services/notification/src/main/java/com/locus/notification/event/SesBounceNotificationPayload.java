package com.locus.notification.event;

// A deliberately minimal local stub of what a real SES bounce/complaint notification carries —
// per frd.md, the real SNS subscription to SES is Part 2 infrastructure; this pass builds the
// consumer logic against a stand-in topic this service can be tested against locally
// (aws sns publish), same as every other event in this project's local verification.
public record SesBounceNotificationPayload(String email, String bounceType) {
}

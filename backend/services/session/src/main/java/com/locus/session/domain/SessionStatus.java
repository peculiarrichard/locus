package com.locus.session.domain;

// The session lifecycle states, per frd.md: active/paused are non-terminal, one per user max.
public enum SessionStatus {
  ACTIVE, PAUSED, COMPLETED, ABANDONED
}

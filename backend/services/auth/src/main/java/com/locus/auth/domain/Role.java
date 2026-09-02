package com.locus.auth.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

// JPA entity for the roles table — a join table target, not a fixed enum, so adding roles later isn't a migration.
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  protected Role() {
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  // Identity is the persistent id, not object identity — two loaded instances of
  // the same row must compare equal.
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Role role)) {
      return false;
    }
    return id != null && id.equals(role.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}

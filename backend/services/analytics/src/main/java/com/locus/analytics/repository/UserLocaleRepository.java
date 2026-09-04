package com.locus.analytics.repository;

import com.locus.analytics.domain.UserLocale;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLocaleRepository extends JpaRepository<UserLocale, UUID> {

  void deleteByUserId(UUID userId);
}

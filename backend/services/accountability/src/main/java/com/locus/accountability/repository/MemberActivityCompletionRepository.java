package com.locus.accountability.repository;

import com.locus.accountability.domain.MemberActivityCompletion;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberActivityCompletionRepository
    extends
      JpaRepository<MemberActivityCompletion, MemberActivityCompletion.Key> {

  boolean existsByUserIdAndCompletedDate(UUID userId, LocalDate completedDate);

  void deleteByUserId(UUID userId);
}

package com.locus.notification.repository;

import com.locus.notification.domain.PendingPartnerActivity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingPartnerActivityRepository extends JpaRepository<PendingPartnerActivity, UUID> {

  List<PendingPartnerActivity> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}

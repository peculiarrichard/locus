package com.locus.notification.repository;

import com.locus.notification.domain.LastSessionActivity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LastSessionActivityRepository extends JpaRepository<LastSessionActivity, UUID> {
}

package com.locus.notification.repository;

import com.locus.notification.domain.UserContact;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContactRepository extends JpaRepository<UserContact, UUID> {

  Optional<UserContact> findByEmail(String email);
}

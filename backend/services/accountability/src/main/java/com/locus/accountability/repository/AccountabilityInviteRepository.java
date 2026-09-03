package com.locus.accountability.repository;

import com.locus.accountability.domain.AccountabilityInvite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountabilityInviteRepository extends JpaRepository<AccountabilityInvite, UUID> {

  Optional<AccountabilityInvite> findByCode(String code);
}

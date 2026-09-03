package com.locus.accountability.repository;

import com.locus.accountability.domain.AccountabilityGroup;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountabilityGroupRepository extends JpaRepository<AccountabilityGroup, UUID> {
}

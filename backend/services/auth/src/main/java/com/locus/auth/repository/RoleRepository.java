package com.locus.auth.repository;

import com.locus.auth.domain.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data repository for roles.
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);
}

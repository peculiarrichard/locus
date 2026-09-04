package com.locus.auth.service;

import com.locus.auth.domain.Role;
import com.locus.auth.repository.RoleRepository;
import com.locus.auth.repository.UserRepository;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Grants the admin role to the env-configured bootstrap email once that account exists — the only
// path to admin, by design; there is no runtime promotion endpoint (technical-spec.md §1).
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Value("${locus.admin.bootstrap-email}")
  private String bootstrapEmail;

  public AdminBootstrapRunner(UserRepository userRepository, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    userRepository.findByEmail(bootstrapEmail).ifPresent(user -> {
      Role adminRole = roleRepository.findByName("admin")
          .orElseThrow(() -> new IllegalStateException("Seed role 'admin' is missing"));
      if (!user.getRoles().contains(adminRole)) {
        var roles = new HashSet<>(user.getRoles());
        roles.add(adminRole);
        user.setRoles(roles);
        userRepository.save(user);
      }
    });
  }
}

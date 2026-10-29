package com.realestate.backend.config;

import com.realestate.backend.config.properties.BootstrapAdminProperties;
import com.realestate.backend.entity.RoleEntity;
import com.realestate.backend.entity.UserEntity;
import com.realestate.backend.enums.Role;
import com.realestate.backend.repository.RoleRepository;
import com.realestate.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;
    private final RoleRepository roleRepository;


    @Override
    public void run(ApplicationArguments args) {

        String configuredEmail = bootstrapAdminProperties.email();
        String configuredPassword = bootstrapAdminProperties.password();

        if (configuredEmail == null || configuredEmail.isBlank()
                || configuredPassword == null || configuredPassword.isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap administrator credentials are not configured"
            );
        }

        String email = configuredEmail.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            log.info("Bootstrap administrator already exists");
            return;
        }

        Set<RoleEntity> roles = roleRepository.findAllByRoleNameIn(
                Set.of(Role.CLIENT, Role.SUPER_ADMIN)
        );

        if (roles.size() != 2) {
            throw new IllegalStateException(
                    "Required bootstrap administrator roles are missing"
            );
        }

        UserEntity superAdmin = UserEntity.builder()
                .fullName("System admin")
                .email(email)
                .passwordHash(passwordEncoder.encode(configuredPassword))
                .roles(roles)
                .build();

        userRepository.save(superAdmin);

        log.info("Bootstrap administrator created");
    }
}

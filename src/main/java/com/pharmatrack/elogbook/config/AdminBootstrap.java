package com.pharmatrack.elogbook.config;

import com.pharmatrack.elogbook.domain.entity.UserEntity;
import com.pharmatrack.elogbook.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.default-password:admin}")
    private String defaultAdminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.findByUsernameIgnoreCase(adminUsername).ifPresent(admin -> {
            if (admin.getPasswordHash() == null || admin.getPasswordHash().isBlank()) {
                admin.setPasswordHash(passwordEncoder.encode(defaultAdminPassword));
                admin.setMustChangePassword(true);
                userRepository.save(admin);
                log.warn("Seeded password for admin user '{}' — default is '{}'. "
                        + "User MUST change it on first login via POST /api/auth/change-password.",
                        adminUsername, defaultAdminPassword);
            }
        });
    }
}

package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.ChangePasswordRequest;
import com.pharmatrack.elogbook.api.dto.LoginRequest;
import com.pharmatrack.elogbook.api.dto.LoginResponse;
import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.api.dto.UserDto;
import com.pharmatrack.elogbook.audit.AuditService;
import com.pharmatrack.elogbook.domain.entity.UserEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.domain.enums.UserStatus;
import com.pharmatrack.elogbook.domain.repository.UserRepository;
import com.pharmatrack.elogbook.exception.BadRequestException;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.exception.UnauthorizedException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import com.pharmatrack.elogbook.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       AuditService auditService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LoginResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(req.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account inactive");
        }
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            // No password set on record — refuse rather than silently accept any input.
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.issue(user);
        AuthPrincipal who = new AuthPrincipal(user.getId(), user.getUsername(), user.getRole(), token);
        auditService.record(who, EntityType.SYSTEM, user.getId().toString(),
                AuditAction.LOGIN, null, null, "User login");
        return new LoginResponse(Mappers.toDto(user), token);
    }

    public void logout(AuthPrincipal who) {
        if (who != null && who.token() != null) {
            jwtService.revoke(who.token());
        }
    }

    @Transactional
    public UserDto changePassword(AuthPrincipal who, ChangePasswordRequest req) {
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BadRequestException("New password and confirmation do not match");
        }
        if (req.newPassword().equals(req.oldPassword())) {
            throw new BadRequestException("New password must differ from the old password");
        }

        UserEntity user = userRepository.findById(who.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        UserDto before = Mappers.toDto(user);
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        user.setMustChangePassword(false);
        UserEntity saved = userRepository.save(user);

        auditService.record(who, EntityType.USER, saved.getId().toString(),
                AuditAction.UPDATE, before, Mappers.toDto(saved), "Self-service password change");

        // Force re-authentication with the new password.
        if (who.token() != null) {
            jwtService.revoke(who.token());
        }
        return Mappers.toDto(saved);
    }
}

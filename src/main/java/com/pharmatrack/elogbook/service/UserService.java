package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.api.dto.PublicUserDto;
import com.pharmatrack.elogbook.api.dto.UserCreateRequest;
import com.pharmatrack.elogbook.api.dto.UserDto;
import com.pharmatrack.elogbook.api.dto.UserUpdateRequest;
import com.pharmatrack.elogbook.audit.AuditService;
import com.pharmatrack.elogbook.domain.entity.UserEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.domain.enums.UserStatus;
import com.pharmatrack.elogbook.domain.repository.UserRepository;
import com.pharmatrack.elogbook.exception.ConflictException;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       AuditService auditService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll().stream().map(Mappers::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PublicUserDto> publicList() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .map(Mappers::toPublic)
                .toList();
    }

    @Transactional
    public UserDto create(AuthPrincipal who, UserCreateRequest req) {
        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw new ConflictException("Username already exists");
        }
        UserEntity entity = UserEntity.builder()
                .username(req.username().toLowerCase())
                .fullName(req.fullName())
                .role(req.role())
                .status(UserStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(req.password()))
                .mustChangePassword(true)
                .build();
        UserEntity saved = userRepository.save(entity);
        auditService.record(who, EntityType.USER, saved.getId().toString(),
                AuditAction.CREATE, null, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public UserDto update(AuthPrincipal who, UUID id, UserUpdateRequest req) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDto before = Mappers.toDto(user);
        if (req.fullName() != null) user.setFullName(req.fullName());
        if (req.role() != null) user.setRole(req.role());
        if (req.status() != null) user.setStatus(req.status());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            // Admin-initiated reset → user must rotate on next login.
            user.setMustChangePassword(true);
        }
        UserEntity saved = userRepository.save(user);
        auditService.record(who, EntityType.USER, saved.getId().toString(),
                AuditAction.UPDATE, before, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public void softDelete(AuthPrincipal who, UUID id, String reason) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        UserDto before = Mappers.toDto(user);
        user.setStatus(UserStatus.INACTIVE);
        UserEntity saved = userRepository.save(user);
        auditService.record(who, EntityType.USER, saved.getId().toString(),
                AuditAction.DELETE, before, Mappers.toDto(saved), reason);
    }
}

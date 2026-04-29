package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.UserRole;
import com.pharmatrack.elogbook.domain.enums.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String fullName,
        UserRole role,
        UserStatus status,
        boolean mustChangePassword,
        OffsetDateTime createdAt
) {}

package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.UserRole;
import com.pharmatrack.elogbook.domain.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        String fullName,
        UserRole role,
        UserStatus status,
        @Size(min = 6, max = 100) String password,
        @NotBlank String reason
) {}

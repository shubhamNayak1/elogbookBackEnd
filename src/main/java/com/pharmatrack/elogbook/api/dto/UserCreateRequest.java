package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Pattern(regexp = "^[a-z0-9]+$") String username,
        @NotBlank String fullName,
        @NotNull UserRole role,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank String reason
) {}

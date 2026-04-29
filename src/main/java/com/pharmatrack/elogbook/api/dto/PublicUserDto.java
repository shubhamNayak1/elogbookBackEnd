package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.UserRole;

import java.util.UUID;

public record PublicUserDto(UUID id, String username, String fullName, UserRole role) {}

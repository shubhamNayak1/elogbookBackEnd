package com.pharmatrack.elogbook.security;

import com.pharmatrack.elogbook.domain.enums.UserRole;

import java.util.UUID;

public record AuthPrincipal(UUID userId, String username, UserRole role, String token) {
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}

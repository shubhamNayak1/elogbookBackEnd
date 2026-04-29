package com.pharmatrack.elogbook.security;

import com.pharmatrack.elogbook.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {
    public AuthPrincipal require() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthPrincipal p) {
            return p;
        }
        throw new UnauthorizedException("Authentication required");
    }
}

package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.ChangePasswordRequest;
import com.pharmatrack.elogbook.api.dto.LoginRequest;
import com.pharmatrack.elogbook.api.dto.LoginResponse;
import com.pharmatrack.elogbook.api.dto.UserDto;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout(currentUser.require());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public UserDto changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        return authService.changePassword(currentUser.require(), req);
    }
}

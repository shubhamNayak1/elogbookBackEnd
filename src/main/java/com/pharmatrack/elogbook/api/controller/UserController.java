package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.PublicUserDto;
import com.pharmatrack.elogbook.api.dto.ReasonRequest;
import com.pharmatrack.elogbook.api.dto.UserCreateRequest;
import com.pharmatrack.elogbook.api.dto.UserDto;
import com.pharmatrack.elogbook.api.dto.UserUpdateRequest;
import com.pharmatrack.elogbook.exception.ForbiddenException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping("/public")
    public List<PublicUserDto> publicList() {
        return userService.publicList();
    }

    @GetMapping
    public List<UserDto> list() {
        requireAdmin();
        return userService.list();
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest req) {
        AuthPrincipal who = requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(who, req));
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest req) {
        AuthPrincipal who = requireAdmin();
        return userService.update(who, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @Valid @RequestBody ReasonRequest req) {
        AuthPrincipal who = requireAdmin();
        userService.softDelete(who, id, req.reason());
        return ResponseEntity.noContent().build();
    }

    private AuthPrincipal requireAdmin() {
        AuthPrincipal who = currentUser.require();
        if (!who.isAdmin()) throw new ForbiddenException("Admin role required");
        return who;
    }
}

package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.LogbookTemplateDto;
import com.pharmatrack.elogbook.api.dto.LogbookTemplateRequest;
import com.pharmatrack.elogbook.api.dto.ReasonRequest;
import com.pharmatrack.elogbook.domain.enums.LogbookStatus;
import com.pharmatrack.elogbook.exception.ForbiddenException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.LogbookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logbooks")
public class LogbookController {

    private final LogbookService logbookService;
    private final CurrentUser currentUser;

    public LogbookController(LogbookService logbookService, CurrentUser currentUser) {
        this.logbookService = logbookService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<LogbookTemplateDto> list(@RequestParam(required = false) LogbookStatus status) {
        currentUser.require();
        return logbookService.list(status);
    }

    @GetMapping("/{id}")
    public LogbookTemplateDto get(@PathVariable String id) {
        currentUser.require();
        return logbookService.get(id);
    }

    @PostMapping
    public ResponseEntity<LogbookTemplateDto> create(@Valid @RequestBody LogbookTemplateRequest req) {
        AuthPrincipal who = requireAdmin();
        return ResponseEntity.status(HttpStatus.CREATED).body(logbookService.create(who, req));
    }

    @PutMapping("/{id}")
    public LogbookTemplateDto update(@PathVariable String id, @Valid @RequestBody LogbookTemplateRequest req) {
        AuthPrincipal who = requireAdmin();
        return logbookService.update(who, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @Valid @RequestBody ReasonRequest req) {
        AuthPrincipal who = requireAdmin();
        logbookService.softDelete(who, id, req.reason());
        return ResponseEntity.noContent().build();
    }

    private AuthPrincipal requireAdmin() {
        AuthPrincipal who = currentUser.require();
        if (!who.isAdmin()) throw new ForbiddenException("Admin role required");
        return who;
    }
}

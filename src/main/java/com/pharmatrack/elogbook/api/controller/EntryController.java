package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.LogbookEntryCreateRequest;
import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.api.dto.LogbookEntryUpdateRequest;
import com.pharmatrack.elogbook.api.dto.ReasonRequest;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.EntryService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private final EntryService entryService;
    private final CurrentUser currentUser;

    public EntryController(EntryService entryService, CurrentUser currentUser) {
        this.entryService = entryService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<LogbookEntryDto> list(
            @RequestParam(required = false) String logbookId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        return entryService.search(currentUser.require(), logbookId, startDate, endDate);
    }

    @PostMapping
    public ResponseEntity<LogbookEntryDto> create(@Valid @RequestBody LogbookEntryCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(entryService.create(currentUser.require(), req));
    }

    @PutMapping("/{id}")
    public LogbookEntryDto update(@PathVariable String id, @Valid @RequestBody LogbookEntryUpdateRequest req) {
        return entryService.update(currentUser.require(), id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @Valid @RequestBody ReasonRequest req) {
        entryService.softDelete(currentUser.require(), id, req.reason());
        return ResponseEntity.noContent().build();
    }
}

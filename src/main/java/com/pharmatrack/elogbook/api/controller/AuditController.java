package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.AuditExportRequest;
import com.pharmatrack.elogbook.api.dto.AuditRecordDto;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.security.CurrentUser;
import com.pharmatrack.elogbook.service.AuditQueryService;
import com.pharmatrack.elogbook.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditQueryService auditQueryService;
    private final ReportService reportService;
    private final CurrentUser currentUser;

    public AuditController(AuditQueryService auditQueryService, ReportService reportService, CurrentUser currentUser) {
        this.auditQueryService = auditQueryService;
        this.reportService = reportService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<AuditRecordDto> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AuditAction action) {
        return auditQueryService.search(currentUser.require(), startDate, endDate, search, action);
    }

    @GetMapping("/{id}")
    public AuditRecordDto get(@PathVariable String id) {
        return auditQueryService.get(currentUser.require(), id);
    }

    @PostMapping("/export")
    public ResponseEntity<String> export(@Valid @RequestBody AuditExportRequest req) {
        String csv = reportService.exportAuditCsv(currentUser.require(), req.startDate(), req.endDate());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}

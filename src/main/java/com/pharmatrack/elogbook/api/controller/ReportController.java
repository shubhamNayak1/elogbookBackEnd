package com.pharmatrack.elogbook.api.controller;

import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.api.dto.ReportExportRequest;
import com.pharmatrack.elogbook.security.CurrentUser;
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
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final CurrentUser currentUser;

    public ReportController(ReportService reportService, CurrentUser currentUser) {
        this.reportService = reportService;
        this.currentUser = currentUser;
    }

    @GetMapping("/entries")
    public List<LogbookEntryDto> entries(
            @RequestParam String logbookId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        return reportService.reportEntries(currentUser.require(), logbookId, startDate, endDate);
    }

    @PostMapping("/export")
    public ResponseEntity<String> export(@Valid @RequestBody ReportExportRequest req) {
        String csv = reportService.exportEntriesCsv(currentUser.require(), req.logbookId(), req.startDate(), req.endDate());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + req.logbookId() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}

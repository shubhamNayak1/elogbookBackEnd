package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.audit.AuditService;
import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReportService {

    private final EntryService entryService;
    private final LogbookService logbookService;
    private final CsvExportService csvExportService;
    private final AuditQueryService auditQueryService;
    private final AuditService auditService;

    public ReportService(EntryService entryService,
                         LogbookService logbookService,
                         CsvExportService csvExportService,
                         AuditQueryService auditQueryService,
                         AuditService auditService) {
        this.entryService = entryService;
        this.logbookService = logbookService;
        this.csvExportService = csvExportService;
        this.auditQueryService = auditQueryService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LogbookEntryDto> reportEntries(AuthPrincipal who, String logbookId,
                                               OffsetDateTime startDate, OffsetDateTime endDate) {
        auditQueryService.assertRange(startDate, endDate);
        return entryService.search(who, logbookId, startDate, endDate);
    }

    @Transactional
    public String exportEntriesCsv(AuthPrincipal who, String logbookId,
                                   OffsetDateTime startDate, OffsetDateTime endDate) {
        auditQueryService.assertRange(startDate, endDate);
        LogbookTemplateEntity tpl = logbookService.load(logbookId);
        List<LogbookEntryDto> entries = entryService.search(who, logbookId, startDate, endDate);
        auditService.record(who, EntityType.LOGBOOK_TEMPLATE, logbookId,
                AuditAction.VIEW_REPORT, null,
                java.util.Map.of("logbookId", logbookId, "startDate", startDate.toString(), "endDate", endDate.toString()),
                "Report export");
        return csvExportService.entriesCsv(tpl, entries);
    }

    @Transactional
    public String exportAuditCsv(AuthPrincipal who, OffsetDateTime startDate, OffsetDateTime endDate) {
        auditQueryService.assertRange(startDate, endDate);
        var records = auditQueryService.search(who, startDate, endDate, null, null);
        auditService.record(who, EntityType.SYSTEM, "audit",
                AuditAction.VIEW_REPORT, null,
                java.util.Map.of("startDate", startDate.toString(), "endDate", endDate.toString()),
                "Audit export");
        return csvExportService.auditCsv(records);
    }
}

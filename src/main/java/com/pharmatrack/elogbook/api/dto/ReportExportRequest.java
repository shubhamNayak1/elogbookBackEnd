package com.pharmatrack.elogbook.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ReportExportRequest(
        @NotBlank String logbookId,
        @NotNull OffsetDateTime startDate,
        @NotNull OffsetDateTime endDate,
        String format
) {}

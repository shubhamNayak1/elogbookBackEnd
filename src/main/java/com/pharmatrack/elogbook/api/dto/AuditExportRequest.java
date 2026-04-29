package com.pharmatrack.elogbook.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record AuditExportRequest(
        @NotNull OffsetDateTime startDate,
        @NotNull OffsetDateTime endDate,
        String format
) {}

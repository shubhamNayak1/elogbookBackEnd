package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.EntryStatus;

import java.time.OffsetDateTime;
import java.util.Map;

public record LogbookEntryDto(
        String id,
        String logbookId,
        Map<String, Object> values,
        OffsetDateTime createdAt,
        String createdBy,
        EntryStatus status,
        String reason
) {}

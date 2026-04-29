package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.LogbookStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record LogbookTemplateDto(
        String id,
        String name,
        String description,
        LogbookStatus status,
        List<LogbookColumnDto> columns,
        OffsetDateTime createdAt,
        String createdBy
) {}

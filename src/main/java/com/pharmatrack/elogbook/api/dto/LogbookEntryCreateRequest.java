package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.EntryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record LogbookEntryCreateRequest(
        @NotBlank String logbookId,
        @NotNull Map<String, Object> values,
        EntryStatus status,
        @NotBlank String reason
) {}

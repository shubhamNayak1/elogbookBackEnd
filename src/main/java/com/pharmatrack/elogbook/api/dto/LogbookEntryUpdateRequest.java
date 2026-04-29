package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.EntryStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record LogbookEntryUpdateRequest(
        Map<String, Object> values,
        EntryStatus status,
        @NotBlank String reason
) {}

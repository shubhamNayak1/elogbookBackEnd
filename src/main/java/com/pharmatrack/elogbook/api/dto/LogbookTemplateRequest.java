package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.LogbookStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LogbookTemplateRequest(
        @NotBlank String name,
        String description,
        LogbookStatus status,
        @NotNull @Valid List<LogbookColumnDto> columns,
        @NotBlank String reason,
        Boolean migrate
) {}

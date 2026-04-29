package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.ColumnType;

import java.util.List;

public record LogbookColumnDto(
        String id,
        String label,
        String key,
        ColumnType type,
        boolean isMandatory,
        List<String> options,
        int displayOrder,
        boolean isSystemManaged,
        String groupName
) {}

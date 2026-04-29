package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditRecordDto(
        String id,
        EntityType entityType,
        String entityId,
        AuditAction action,
        Object oldValue,
        Object newValue,
        UUID userId,
        String username,
        OffsetDateTime timestamp,
        String reason
) {}

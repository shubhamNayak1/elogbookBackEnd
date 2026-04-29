package com.pharmatrack.elogbook.api.dto;

import com.pharmatrack.elogbook.domain.entity.AuditRecordEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookColumnEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookEntryEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import com.pharmatrack.elogbook.domain.entity.UserEntity;

public final class Mappers {
    private Mappers() {}

    public static UserDto toDto(UserEntity u) {
        return new UserDto(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.getStatus(),
                u.isMustChangePassword(), u.getCreatedAt());
    }

    public static PublicUserDto toPublic(UserEntity u) {
        return new PublicUserDto(u.getId(), u.getUsername(), u.getFullName(), u.getRole());
    }

    public static LogbookColumnDto toDto(LogbookColumnEntity c) {
        return new LogbookColumnDto(
                c.getId(), c.getLabel(), c.getKey(), c.getType(), c.isMandatory(),
                c.getOptions(), c.getDisplayOrder(), c.isSystemManaged(), c.getGroupName());
    }

    public static LogbookTemplateDto toDto(LogbookTemplateEntity t) {
        return new LogbookTemplateDto(
                t.getId(), t.getName(), t.getDescription(), t.getStatus(),
                t.orderedColumns().stream().map(Mappers::toDto).toList(),
                t.getCreatedAt(), t.getCreatedBy());
    }

    public static LogbookEntryDto toDto(LogbookEntryEntity e) {
        return new LogbookEntryDto(e.getId(), e.getLogbookId(), e.getValues(), e.getCreatedAt(),
                e.getCreatedBy(), e.getStatus(), e.getReason());
    }

    public static AuditRecordDto toDto(AuditRecordEntity a) {
        return new AuditRecordDto(a.getId(), a.getEntityType(), a.getEntityId(), a.getAction(),
                a.getOldValue(), a.getNewValue(), a.getUserId(), a.getUsername(),
                a.getTimestamp(), a.getReason());
    }
}

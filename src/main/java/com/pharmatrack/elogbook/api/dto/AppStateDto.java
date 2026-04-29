package com.pharmatrack.elogbook.api.dto;

import java.util.List;

public record AppStateDto(
        UserDto currentUser,
        List<UserDto> users,
        List<LogbookTemplateDto> logbooks,
        List<LogbookEntryDto> entries,
        List<AuditRecordDto> auditLogs
) {}

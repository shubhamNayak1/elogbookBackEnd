package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.AppStateDto;
import com.pharmatrack.elogbook.api.dto.AuditRecordDto;
import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.api.dto.LogbookTemplateDto;
import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.api.dto.UserDto;
import com.pharmatrack.elogbook.domain.repository.UserRepository;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class StateService {

    private final UserRepository userRepository;
    private final LogbookService logbookService;
    private final EntryService entryService;
    private final AuditQueryService auditQueryService;

    public StateService(UserRepository userRepository,
                        LogbookService logbookService,
                        EntryService entryService,
                        AuditQueryService auditQueryService) {
        this.userRepository = userRepository;
        this.logbookService = logbookService;
        this.entryService = entryService;
        this.auditQueryService = auditQueryService;
    }

    @Transactional(readOnly = true)
    public AppStateDto bootstrap(AuthPrincipal who) {
        UserDto current = userRepository.findById(who.userId())
                .map(Mappers::toDto)
                .orElseThrow(() -> new NotFoundException("Current user not found"));

        List<UserDto> users = who.isAdmin()
                ? userRepository.findAll().stream().map(Mappers::toDto).toList()
                : List.of(current);

        List<LogbookTemplateDto> logbooks = logbookService.list(null);
        List<LogbookEntryDto> entries = entryService.search(who, null, null, null);
        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusDays(31);
        List<AuditRecordDto> audit = auditQueryService.search(who, start, end, null, null);

        return new AppStateDto(current, users, logbooks, entries, audit);
    }
}

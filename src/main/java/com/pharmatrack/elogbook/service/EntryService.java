package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.LogbookEntryCreateRequest;
import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.api.dto.LogbookEntryUpdateRequest;
import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.audit.AuditService;
import com.pharmatrack.elogbook.domain.entity.LogbookColumnEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookEntryEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import com.pharmatrack.elogbook.domain.entity.UserEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.ColumnType;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.domain.enums.EntryStatus;
import com.pharmatrack.elogbook.domain.repository.LogbookEntryRepository;
import com.pharmatrack.elogbook.domain.repository.UserRepository;
import com.pharmatrack.elogbook.exception.BadRequestException;
import com.pharmatrack.elogbook.exception.ConflictException;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EntryService {

    private final LogbookEntryRepository repo;
    private final LogbookService logbookService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public EntryService(LogbookEntryRepository repo,
                        LogbookService logbookService,
                        UserRepository userRepository,
                        AuditService auditService) {
        this.repo = repo;
        this.logbookService = logbookService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LogbookEntryDto> search(AuthPrincipal who, String logbookId,
                                        OffsetDateTime startDate, OffsetDateTime endDate) {
        return repo.search(logbookId, startDate, endDate, who.isAdmin() ? null : who.userId())
                .stream().map(Mappers::toDto).toList();
    }

    @Transactional
    public LogbookEntryDto create(AuthPrincipal who, LogbookEntryCreateRequest req) {
        LogbookTemplateEntity tpl = logbookService.load(req.logbookId());
        Map<String, Object> values = validateAndInjectSystem(tpl, req.values());

        UserEntity user = userRepository.findById(who.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        LogbookEntryEntity entity = LogbookEntryEntity.builder()
                .id("ent_" + System.currentTimeMillis())
                .logbookId(tpl.getId())
                .values(values)
                .createdBy(user.getFullName())
                .createdByUserId(user.getId())
                .status(req.status() == null ? EntryStatus.SUBMITTED : req.status())
                .reason(req.reason())
                .build();

        LogbookEntryEntity saved = repo.save(entity);
        auditService.record(who, EntityType.LOGBOOK_ENTRY, saved.getId(),
                AuditAction.CREATE, null, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public LogbookEntryDto update(AuthPrincipal who, String id, LogbookEntryUpdateRequest req) {
        LogbookEntryEntity entry = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        if (!who.isAdmin() && !entry.getCreatedByUserId().equals(who.userId())) {
            throw new com.pharmatrack.elogbook.exception.ForbiddenException("Cannot edit other user's entry");
        }
        if (entry.getStatus() == EntryStatus.SIGNED) {
            throw new ConflictException("Signed entries are immutable");
        }
        LogbookEntryDto before = Mappers.toDto(entry);
        if (req.values() != null) {
            LogbookTemplateEntity tpl = logbookService.load(entry.getLogbookId());
            entry.setValues(validateAndInjectSystem(tpl, req.values()));
        }
        if (req.status() != null) entry.setStatus(req.status());
        entry.setReason(req.reason());
        LogbookEntryEntity saved = repo.save(entry);
        auditService.record(who, EntityType.LOGBOOK_ENTRY, saved.getId(),
                AuditAction.UPDATE, before, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public void softDelete(AuthPrincipal who, String id, String reason) {
        LogbookEntryEntity entry = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Entry not found"));
        if (!who.isAdmin() && !entry.getCreatedByUserId().equals(who.userId())) {
            throw new com.pharmatrack.elogbook.exception.ForbiddenException("Cannot delete other user's entry");
        }
        LogbookEntryDto before = Mappers.toDto(entry);
        entry.setStatus(EntryStatus.DELETED);
        entry.setReason(reason);
        LogbookEntryEntity saved = repo.save(entry);
        auditService.record(who, EntityType.LOGBOOK_ENTRY, saved.getId(),
                AuditAction.DELETE, before, Mappers.toDto(saved), reason);
    }

    Map<String, Object> validateAndInjectSystem(LogbookTemplateEntity tpl, Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>(input == null ? Map.of() : input);

        for (LogbookColumnEntity col : tpl.orderedColumns()) {
            if (col.isSystemManaged() && LogbookService.SYSTEM_TIME_KEY.equalsIgnoreCase(col.getKey())) {
                result.put(col.getKey(), OffsetDateTime.now(ZoneOffset.UTC).toString());
                continue;
            }
            Object value = result.get(col.getKey());
            if (col.isMandatory() && (value == null || (value instanceof String s && s.isBlank()))) {
                throw new BadRequestException("Missing mandatory field: " + col.getKey());
            }
            if (value == null) continue;
            validateType(col, value);
        }
        return result;
    }

    private void validateType(LogbookColumnEntity col, Object value) {
        switch (col.getType()) {
            case NUMBER -> {
                if (!(value instanceof Number) && !(value instanceof String s && s.matches("-?\\d+(\\.\\d+)?"))) {
                    throw new BadRequestException("Field '" + col.getKey() + "' must be a number");
                }
            }
            case BOOLEAN -> {
                if (!(value instanceof Boolean)
                        && !(value instanceof String s && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false")))) {
                    throw new BadRequestException("Field '" + col.getKey() + "' must be a boolean");
                }
            }
            case DROPDOWN -> {
                if (col.getOptions() == null || !col.getOptions().contains(String.valueOf(value))) {
                    throw new BadRequestException("Field '" + col.getKey() + "' value not in dropdown options");
                }
            }
            case TEXT, DATE, TIME, DATETIME -> {
                if (!(value instanceof String)) {
                    throw new BadRequestException("Field '" + col.getKey() + "' must be a string");
                }
            }
        }
    }
}

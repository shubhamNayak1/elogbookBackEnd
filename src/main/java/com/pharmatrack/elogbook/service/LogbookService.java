package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.LogbookColumnDto;
import com.pharmatrack.elogbook.api.dto.LogbookTemplateDto;
import com.pharmatrack.elogbook.api.dto.LogbookTemplateRequest;
import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.audit.AuditService;
import com.pharmatrack.elogbook.domain.entity.LogbookColumnEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.ColumnType;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.domain.enums.LogbookStatus;
import com.pharmatrack.elogbook.domain.repository.LogbookTemplateRepository;
import com.pharmatrack.elogbook.exception.BadRequestException;
import com.pharmatrack.elogbook.exception.ConflictException;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LogbookService {

    public static final String SYSTEM_TIME_KEY = "time";

    private final LogbookTemplateRepository repo;
    private final AuditService auditService;

    public LogbookService(LogbookTemplateRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<LogbookTemplateDto> list(LogbookStatus status) {
        List<LogbookTemplateEntity> templates = (status == null)
                ? repo.findAll()
                : repo.findAllByStatus(status);
        return templates.stream().map(Mappers::toDto).toList();
    }

    @Transactional(readOnly = true)
    public LogbookTemplateDto get(String id) {
        return Mappers.toDto(load(id));
    }

    LogbookTemplateEntity load(String id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Template not found"));
    }

    @Transactional
    public LogbookTemplateDto create(AuthPrincipal who, LogbookTemplateRequest req) {
        validateColumns(req.columns(), false, null);
        String id = "lb_" + System.currentTimeMillis();
        LogbookTemplateEntity entity = LogbookTemplateEntity.builder()
                .id(id)
                .name(req.name())
                .description(req.description())
                .status(req.status() == null ? LogbookStatus.ACTIVE : req.status())
                .createdBy(who.username())
                .build();
        entity.setColumns(buildColumns(id, req.columns()));
        ensureSystemColumn(entity);
        LogbookTemplateEntity saved = repo.save(entity);
        auditService.record(who, EntityType.LOGBOOK_TEMPLATE, saved.getId(),
                AuditAction.CREATE, null, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public LogbookTemplateDto update(AuthPrincipal who, String id, LogbookTemplateRequest req) {
        LogbookTemplateEntity existing = load(id);
        LogbookTemplateDto before = Mappers.toDto(existing);

        boolean migrate = Boolean.TRUE.equals(req.migrate());
        if (existing.getStatus() == LogbookStatus.ACTIVE && !migrate) {
            assertNoBreakingChanges(existing.orderedColumns(), req.columns());
        }
        validateColumns(req.columns(), true, existing);

        existing.setName(req.name());
        existing.setDescription(req.description());
        if (req.status() != null) existing.setStatus(req.status());

        existing.getColumns().clear();
        existing.getColumns().addAll(buildColumns(existing.getId(), req.columns()));
        ensureSystemColumn(existing);

        LogbookTemplateEntity saved = repo.save(existing);
        auditService.record(who, EntityType.LOGBOOK_TEMPLATE, saved.getId(),
                AuditAction.UPDATE, before, Mappers.toDto(saved), req.reason());
        return Mappers.toDto(saved);
    }

    @Transactional
    public void softDelete(AuthPrincipal who, String id, String reason) {
        LogbookTemplateEntity existing = load(id);
        LogbookTemplateDto before = Mappers.toDto(existing);
        existing.setStatus(LogbookStatus.INACTIVE);
        LogbookTemplateEntity saved = repo.save(existing);
        auditService.record(who, EntityType.LOGBOOK_TEMPLATE, saved.getId(),
                AuditAction.DELETE, before, Mappers.toDto(saved), reason);
    }

    private List<LogbookColumnEntity> buildColumns(String logbookId, List<LogbookColumnDto> input) {
        List<LogbookColumnEntity> out = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (LogbookColumnDto c : input) {
            if (!keys.add(c.key())) {
                throw new BadRequestException("Duplicate column key: " + c.key());
            }
            out.add(LogbookColumnEntity.builder()
                    .id(c.id() == null || c.id().isBlank()
                            ? "col_" + logbookId + "_" + c.key()
                            : c.id())
                    .logbookId(logbookId)
                    .label(c.label())
                    .key(c.key())
                    .type(c.type())
                    .mandatory(c.isMandatory())
                    .systemManaged(c.isSystemManaged())
                    .options(c.options())
                    .displayOrder(c.displayOrder())
                    .groupName(c.groupName())
                    .build());
        }
        return out;
    }

    private void ensureSystemColumn(LogbookTemplateEntity template) {
        boolean hasTime = template.getColumns().stream()
                .anyMatch(c -> c.isSystemManaged() && SYSTEM_TIME_KEY.equalsIgnoreCase(c.getKey()));
        if (!hasTime) {
            template.getColumns().add(LogbookColumnEntity.builder()
                    .id("col_" + template.getId() + "_time")
                    .logbookId(template.getId())
                    .label("Time")
                    .key(SYSTEM_TIME_KEY)
                    .type(ColumnType.DATETIME)
                    .mandatory(true)
                    .systemManaged(true)
                    .displayOrder(-1)
                    .build());
        }
    }

    private void validateColumns(List<LogbookColumnDto> cols, boolean isUpdate, LogbookTemplateEntity existing) {
        if (cols == null || cols.isEmpty()) {
            throw new BadRequestException("Template must have at least one column");
        }
        for (LogbookColumnDto c : cols) {
            if (c.key() == null || c.key().isBlank()) {
                throw new BadRequestException("Column key required");
            }
            if (c.label() == null || c.label().isBlank()) {
                throw new BadRequestException("Column label required");
            }
            if (c.type() == ColumnType.DROPDOWN && (c.options() == null || c.options().isEmpty())) {
                throw new BadRequestException("Dropdown column '" + c.key() + "' must have options");
            }
        }
        if (isUpdate && existing != null) {
            Map<String, LogbookColumnEntity> oldByKey = existing.getColumns().stream()
                    .collect(Collectors.toMap(LogbookColumnEntity::getKey, Function.identity()));
            for (LogbookColumnDto c : cols) {
                LogbookColumnEntity old = oldByKey.get(c.key());
                if (old != null && old.isSystemManaged()) {
                    if (!old.getLabel().equals(c.label()) || old.getType() != c.type()
                            || old.getDisplayOrder() != c.displayOrder()) {
                        throw new ConflictException("System-managed column '" + c.key() + "' is immutable");
                    }
                }
            }
        }
    }

    private void assertNoBreakingChanges(List<LogbookColumnEntity> old, List<LogbookColumnDto> next) {
        Map<String, LogbookColumnEntity> oldByKey = old.stream()
                .collect(Collectors.toMap(LogbookColumnEntity::getKey, Function.identity()));
        Set<String> nextKeys = next.stream().map(LogbookColumnDto::key).collect(Collectors.toSet());
        for (LogbookColumnEntity oldCol : old) {
            if (!nextKeys.contains(oldCol.getKey())) {
                throw new ConflictException(
                        "Cannot remove column '" + oldCol.getKey() + "' from ACTIVE template without migration flag");
            }
        }
        for (LogbookColumnDto c : next) {
            LogbookColumnEntity oldCol = oldByKey.get(c.key());
            if (oldCol != null && oldCol.getType() != c.type()) {
                throw new ConflictException(
                        "Cannot change type of column '" + c.key() + "' on ACTIVE template without migration flag");
            }
        }
    }
}

package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.AuditRecordDto;
import com.pharmatrack.elogbook.api.dto.Mappers;
import com.pharmatrack.elogbook.domain.entity.AuditRecordEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.repository.AuditRecordRepository;
import com.pharmatrack.elogbook.exception.BadRequestException;
import com.pharmatrack.elogbook.exception.NotFoundException;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuditQueryService {

    private final AuditRecordRepository repo;

    @Value("${app.audit.range-max-days}")
    private int maxRangeDays;

    public AuditQueryService(AuditRecordRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<AuditRecordDto> search(AuthPrincipal who,
                                       OffsetDateTime startDate,
                                       OffsetDateTime endDate,
                                       String search,
                                       AuditAction action) {
        OffsetDateTime end = endDate == null ? OffsetDateTime.now() : endDate;
        OffsetDateTime start = startDate == null ? end.minusDays(maxRangeDays) : startDate;
        assertRange(start, end);
        return repo.search(start, end, who.isAdmin() ? null : who.userId(), action, search)
                .stream().map(Mappers::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AuditRecordDto get(AuthPrincipal who, String id) {
        AuditRecordEntity rec = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Audit record not found"));
        if (!who.isAdmin() && !rec.getUserId().equals(who.userId())) {
            throw new com.pharmatrack.elogbook.exception.ForbiddenException("Audit record not visible");
        }
        return Mappers.toDto(rec);
    }

    public void assertRange(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null) throw new BadRequestException("startDate and endDate required");
        if (end.isBefore(start)) throw new BadRequestException("endDate must be after startDate");
        if (Duration.between(start, end).toDays() > maxRangeDays) {
            throw new BadRequestException("Range exceeds " + maxRangeDays + " days");
        }
    }
}

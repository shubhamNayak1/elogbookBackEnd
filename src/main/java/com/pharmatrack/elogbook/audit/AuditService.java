package com.pharmatrack.elogbook.audit;

import com.pharmatrack.elogbook.domain.entity.AuditRecordEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import com.pharmatrack.elogbook.domain.repository.AuditRecordRepository;
import com.pharmatrack.elogbook.security.AuthPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditRecordRepository repo;

    public AuditService(AuditRecordRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AuditRecordEntity record(AuthPrincipal who,
                                    EntityType entityType,
                                    String entityId,
                                    AuditAction action,
                                    Object oldValue,
                                    Object newValue,
                                    String reason) {
        if ((reason == null || reason.isBlank())
                && action != AuditAction.LOGIN
                && action != AuditAction.VIEW_REPORT) {
            throw new IllegalArgumentException("reason is required");
        }
        AuditRecordEntity entity = AuditRecordEntity.builder()
                .id("aud_" + System.currentTimeMillis() + "_" + Long.toHexString(Double.doubleToLongBits(Math.random())))
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .userId(who.userId())
                .username(who.username())
                .reason(reason == null ? action.name() : reason)
                .build();
        return repo.save(entity);
    }
}

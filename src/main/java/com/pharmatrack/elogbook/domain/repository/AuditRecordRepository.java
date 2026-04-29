package com.pharmatrack.elogbook.domain.repository;

import com.pharmatrack.elogbook.domain.entity.AuditRecordEntity;
import com.pharmatrack.elogbook.domain.enums.AuditAction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface AuditRecordRepository
        extends JpaRepository<AuditRecordEntity, String>, JpaSpecificationExecutor<AuditRecordEntity> {

    default List<AuditRecordEntity> search(OffsetDateTime startDate,
                                           OffsetDateTime endDate,
                                           UUID userId,
                                           AuditAction action,
                                           String search) {
        return findAll(where(startDate, endDate, userId, action, search),
                Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    static Specification<AuditRecordEntity> where(OffsetDateTime startDate,
                                                  OffsetDateTime endDate,
                                                  UUID userId,
                                                  AuditAction action,
                                                  String search) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (startDate != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            if (userId != null) {
                preds.add(cb.equal(root.get("userId"), userId));
            }
            if (action != null) {
                preds.add(cb.equal(root.get("action"), action));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                preds.add(cb.or(
                        cb.like(cb.lower(root.get("username")), like),
                        cb.like(cb.lower(root.get("entityId")), like),
                        cb.like(cb.lower(root.get("reason")), like)
                ));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
    }
}

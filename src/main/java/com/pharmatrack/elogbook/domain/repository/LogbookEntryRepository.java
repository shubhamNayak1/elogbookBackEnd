package com.pharmatrack.elogbook.domain.repository;

import com.pharmatrack.elogbook.domain.entity.LogbookEntryEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface LogbookEntryRepository
        extends JpaRepository<LogbookEntryEntity, String>, JpaSpecificationExecutor<LogbookEntryEntity> {

    default List<LogbookEntryEntity> search(String logbookId,
                                            OffsetDateTime startDate,
                                            OffsetDateTime endDate,
                                            UUID userId) {
        return findAll(where(logbookId, startDate, endDate, userId), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    static Specification<LogbookEntryEntity> where(String logbookId,
                                                   OffsetDateTime startDate,
                                                   OffsetDateTime endDate,
                                                   UUID userId) {
        return (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            if (logbookId != null) {
                preds.add(cb.equal(root.get("logbookId"), logbookId));
            }
            if (startDate != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            if (userId != null) {
                preds.add(cb.equal(root.get("createdByUserId"), userId));
            }
            return preds.isEmpty() ? cb.conjunction() : cb.and(preds.toArray(new Predicate[0]));
        };
    }
}

package com.pharmatrack.elogbook.domain.entity;

import com.pharmatrack.elogbook.domain.enums.EntryStatus;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "logbook_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogbookEntryEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Column(name = "logbook_id", nullable = false, length = 50)
    private String logbookId;

    @Type(JsonBinaryType.class)
    @Column(name = "values", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> values;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_by_user_id", nullable = false, columnDefinition = "uuid")
    private UUID createdByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryStatus status;

    @Column(columnDefinition = "text")
    private String reason;
}

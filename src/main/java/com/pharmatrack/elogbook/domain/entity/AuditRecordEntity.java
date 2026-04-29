package com.pharmatrack.elogbook.domain.entity;

import com.pharmatrack.elogbook.domain.enums.AuditAction;
import com.pharmatrack.elogbook.domain.enums.EntityType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "audit_records")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditRecordEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false, length = 100)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditAction action;

    @Type(JsonBinaryType.class)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Object oldValue;

    @Type(JsonBinaryType.class)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Object newValue;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime timestamp;
}

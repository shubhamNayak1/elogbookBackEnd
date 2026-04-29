package com.pharmatrack.elogbook.domain.entity;

import com.pharmatrack.elogbook.domain.enums.LogbookStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "logbook_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogbookTemplateEntity {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LogbookStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "logbook_id", nullable = false)
    @Builder.Default
    private List<LogbookColumnEntity> columns = new ArrayList<>();

    public List<LogbookColumnEntity> orderedColumns() {
        List<LogbookColumnEntity> sorted = new ArrayList<>(columns);
        sorted.sort(Comparator.comparingInt(LogbookColumnEntity::getDisplayOrder));
        return sorted;
    }
}

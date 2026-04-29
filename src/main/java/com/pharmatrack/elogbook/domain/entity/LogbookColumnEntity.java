package com.pharmatrack.elogbook.domain.entity;

import com.pharmatrack.elogbook.domain.enums.ColumnType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Entity
@Table(name = "logbook_columns",
        uniqueConstraints = @UniqueConstraint(columnNames = {"logbook_id", "key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogbookColumnEntity {
    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "logbook_id", nullable = false, length = 50, insertable = false, updatable = false)
    private String logbookId;

    @Column(nullable = false)
    private String label;

    @Column(name = "key", nullable = false, length = 100)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ColumnType type;

    @Column(name = "is_mandatory", nullable = false)
    private boolean mandatory;

    @Column(name = "is_system_managed", nullable = false)
    private boolean systemManaged;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> options;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "group_name", length = 100)
    private String groupName;
}

package com.pharmatrack.elogbook.domain.repository;

import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import com.pharmatrack.elogbook.domain.enums.LogbookStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogbookTemplateRepository extends JpaRepository<LogbookTemplateEntity, String> {
    List<LogbookTemplateEntity> findAllByStatus(LogbookStatus status);
}

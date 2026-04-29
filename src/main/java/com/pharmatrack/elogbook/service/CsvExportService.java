package com.pharmatrack.elogbook.service;

import com.pharmatrack.elogbook.api.dto.AuditRecordDto;
import com.pharmatrack.elogbook.api.dto.LogbookEntryDto;
import com.pharmatrack.elogbook.domain.entity.LogbookColumnEntity;
import com.pharmatrack.elogbook.domain.entity.LogbookTemplateEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CsvExportService {

    public String auditCsv(List<AuditRecordDto> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Timestamp,User,Action,Entity Type,Entity ID,Reason\n");
        for (AuditRecordDto r : records) {
            sb.append(escape(r.id())).append(',')
                    .append(escape(r.timestamp() == null ? "" : r.timestamp().toString())).append(',')
                    .append(escape(r.username())).append(',')
                    .append(escape(r.action() == null ? "" : r.action().name())).append(',')
                    .append(escape(r.entityType() == null ? "" : r.entityType().name())).append(',')
                    .append(escape(r.entityId())).append(',')
                    .append(escape(r.reason()))
                    .append('\n');
        }
        return sb.toString();
    }

    public String entriesCsv(LogbookTemplateEntity template, List<LogbookEntryDto> entries) {
        List<LogbookColumnEntity> cols = template.orderedColumns();
        StringBuilder sb = new StringBuilder();
        sb.append("Entry ID,Timestamp,Signee,Justification");
        for (LogbookColumnEntity c : cols) {
            sb.append(',').append(escape(c.getLabel()));
        }
        sb.append('\n');
        for (LogbookEntryDto e : entries) {
            sb.append(escape(e.id())).append(',')
                    .append(escape(e.createdAt() == null ? "" : e.createdAt().toString())).append(',')
                    .append(escape(e.createdBy())).append(',')
                    .append(escape(e.reason()));
            Map<String, Object> v = e.values() == null ? Map.of() : e.values();
            for (LogbookColumnEntity c : cols) {
                sb.append(',').append(escape(v.get(c.getKey()) == null ? "" : v.get(c.getKey()).toString()));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String escaped = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}

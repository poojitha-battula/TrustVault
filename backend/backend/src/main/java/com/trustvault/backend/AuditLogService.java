
package com.trustvault.backend;

import com.trustvault.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actorEmail, String action, String details) {
        AuditLog auditLog = new AuditLog(
                actorEmail,
                action,
                details,
                LocalDateTime.now()
        );

        auditLogRepository.save(auditLog);
    }
}

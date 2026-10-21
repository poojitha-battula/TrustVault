
package com.trustvault.backend;

import com.trustvault.backend.AuditLog;
import com.trustvault.backend.repository.AuditLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAuditLogs(Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN")
                        || role.equals("ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403)
                    .body(Map.of("error",
                            "Only administrators can view audit logs"));
        }

        List<AuditLog> logs = auditLogRepository.findAll();

        return ResponseEntity.ok(logs);
    }
}

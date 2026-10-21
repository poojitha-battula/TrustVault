
package com.trustvault.backend;

import com.trustvault.backend.entity.Document;
import com.trustvault.backend.entity.DocumentPermission;
import com.trustvault.backend.repository.DocumentPermissionRepository;
import com.trustvault.backend.repository.DocumentRepository;
import com.trustvault.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentPermissionController {

    private final DocumentRepository documentRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public DocumentPermissionController(
            DocumentRepository documentRepository,
            DocumentPermissionRepository permissionRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {
        this.documentRepository = documentRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/{documentId}/permissions")
    public ResponseEntity<?> shareDocument(
            @PathVariable Long documentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String ownerEmail = authentication.getName().toLowerCase().trim();
        String userEmail = request.get("userEmail");
        String permission = request.get("permission");

        Document document = documentRepository.findById(documentId)
                .orElse(null);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Document not found"));
        }

        if (!document.getOwnerEmail().equalsIgnoreCase(ownerEmail)) {
            auditLogService.log(ownerEmail, "PERMISSION_DENIED",
                    "Share denied for document ID: " + documentId);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the document owner can share it"));
        }

        if (userEmail == null || userEmail.isBlank()
                || permission == null
                || !(permission.equalsIgnoreCase("READ")
                || permission.equalsIgnoreCase("EDIT"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide userEmail and READ or EDIT permission"));
        }

        userEmail = userEmail.trim().toLowerCase();

        if (!userRepository.existsByEmail(userEmail)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User is not registered"));
        }

        if (userEmail.equals(ownerEmail)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Owner already has access"));
        }

        String finalPermission = permission.toUpperCase();

        DocumentPermission existing = permissionRepository
                .findByDocumentIdAndUserEmail(documentId, userEmail)
                .orElse(null);

        if (existing == null) {
            permissionRepository.save(new DocumentPermission(
                    documentId, userEmail, finalPermission));
        } else {
            existing.setPermission(finalPermission);
            permissionRepository.save(existing);
        }

        auditLogService.log(ownerEmail, "PERMISSION_GRANTED",
                "Granted " + finalPermission + " to " + userEmail
                        + " for document ID: " + documentId);

        return ResponseEntity.ok(Map.of(
                "message", "Permission saved",
                "documentId", documentId,
                "userEmail", userEmail,
                "permission", finalPermission));
    }
    @DeleteMapping("/{documentId}/permissions/{userEmail}")
public ResponseEntity<?> revokePermission(
        @PathVariable Long documentId,
        @PathVariable String userEmail,
        Authentication authentication) {

    String ownerEmail = authentication.getName()
            .trim().toLowerCase();

    userEmail = userEmail.trim().toLowerCase();

    Document document = documentRepository.findById(documentId)
            .orElse(null);

    if (document == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Document not found"));
    }

    if (!document.getOwnerEmail().equalsIgnoreCase(ownerEmail)) {
        auditLogService.log(ownerEmail, "PERMISSION_DENIED",
                "Revoke denied for document ID: " + documentId);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error",
                        "Only the document owner can revoke permission"));
    }

    DocumentPermission permission = permissionRepository
            .findByDocumentIdAndUserEmail(documentId, userEmail)
            .orElse(null);

    if (permission == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Permission not found"));
    }

    permissionRepository.delete(permission);

    auditLogService.log(ownerEmail, "PERMISSION_REVOKED",
            "Revoked permission for " + userEmail
                    + " on document ID: " + documentId);

    return ResponseEntity.ok(Map.of(
            "message", "Permission revoked successfully",
            "documentId", documentId,
            "userEmail", userEmail));
}
}

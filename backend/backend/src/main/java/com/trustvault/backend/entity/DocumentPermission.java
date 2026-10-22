
package com.trustvault.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "document_permissions",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"document_id", "user_email"}
    )
)
public class DocumentPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String permission;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public DocumentPermission() {
    }

    public DocumentPermission(
            Long documentId,
            String userEmail,
            String permission) {
        this.documentId = documentId;
        this.userEmail = userEmail;
        this.permission = permission;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getPermission() {
        return permission;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}

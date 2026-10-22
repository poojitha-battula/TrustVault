
package com.trustvault.backend.repository;

import com.trustvault.backend.entity.DocumentPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentPermissionRepository
        extends JpaRepository<DocumentPermission, Long> {

    List<DocumentPermission> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);

    Optional<DocumentPermission> findByDocumentIdAndUserEmail(
            Long documentId, String userEmail);

    void deleteByDocumentIdAndUserEmail(
            Long documentId, String userEmail);

    boolean existsByDocumentIdAndUserEmailAndPermission(
            Long documentId, String userEmail, String permission);
}

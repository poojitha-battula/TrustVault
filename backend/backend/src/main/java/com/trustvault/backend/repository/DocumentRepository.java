
package com.trustvault.backend.repository;

import com.trustvault.backend.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByOwnerEmail(String ownerEmail);

    List<Document> findByCategoryIgnoreCase(String category);
    List<Document> findByNameContainingIgnoreCase(String name);
      List<Document> findByUploadedAtBetween(
            LocalDateTime start,
            LocalDateTime end);
}

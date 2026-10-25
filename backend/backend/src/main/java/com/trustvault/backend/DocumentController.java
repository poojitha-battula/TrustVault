
package com.trustvault.backend;

import com.trustvault.backend.entity.Document;
import com.trustvault.backend.repository.DocumentRepository;
import com.trustvault.backend.repository.DocumentPermissionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final AuditLogService auditLogService;

    public DocumentController(
        DocumentRepository documentRepository,
        AuditLogService auditLogService,
        DocumentPermissionRepository permissionRepository) {

    this.documentRepository = documentRepository;
    this.auditLogService = auditLogService;
    this.permissionRepository = permissionRepository;
}

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            Authentication authentication) {

        if (file == null || file.isEmpty()
                || category == null || category.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "File and category are required"));
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "File size must not exceed 5 MB"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid filename"));
        }

        String originalName;
        try {
            originalName = Paths.get(originalFilename)
                    .getFileName().toString();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid filename"));
        }

        if (originalName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid filename"));
        }
String lowerFilename = originalName.toLowerCase(
        java.util.Locale.ROOT);

if (!(lowerFilename.endsWith(".pdf")
        || lowerFilename.endsWith(".txt")
        || lowerFilename.endsWith(".png")
        || lowerFilename.endsWith(".jpg")
        || lowerFilename.endsWith(".jpeg"))) {
    return ResponseEntity.badRequest()
            .body(Map.of("error",
                    "Only PDF, TXT, PNG, JPG and JPEG files are allowed"));
}
if (lowerFilename.endsWith(".pdf")) {
    try {
        byte[] header = file.getBytes();

        if (header.length < 5
                || header[0] != '%'
                || header[1] != 'P'
                || header[2] != 'D'
                || header[3] != 'F'
                || header[4] != '-') {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid PDF file content"));
        }
    } catch (IOException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Unable to validate uploaded file"));
    }
}

if (lowerFilename.endsWith(".png")
        || lowerFilename.endsWith(".jpg")
        || lowerFilename.endsWith(".jpeg")) {
    try {
        byte[] header = file.getBytes();

        boolean validPng = header.length >= 8
                && (header[0] & 0xFF) == 0x89
                && header[1] == 0x50
                && header[2] == 0x4E
                && header[3] == 0x47
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;

        boolean validJpeg = header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF;

        if (!validPng && !validJpeg) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Invalid image file content"));
        }
    } catch (IOException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Unable to validate uploaded file"));
    }
}

if (lowerFilename.endsWith(".txt")) {
    try {
        byte[] content = file.getBytes();

        for (byte value : content) {
            if (value == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error",
                                "Invalid text file content"));
            }
        }

        java.nio.charset.StandardCharsets.UTF_8
                .newDecoder()
                .decode(java.nio.ByteBuffer.wrap(content));

    } catch (java.nio.charset.CharacterCodingException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Text file must contain valid UTF-8 text"));
    } catch (IOException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error",
                        "Unable to validate uploaded file"));
    }
}



        Path uploadDirectory =
                Paths.get("uploads").toAbsolutePath().normalize();
        Path destination = null;

        try {
            Files.createDirectories(uploadDirectory);

            String storedName = UUID.randomUUID() + "_" + originalName;
            destination = uploadDirectory.resolve(storedName).normalize();

            if (!destination.startsWith(uploadDirectory)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid filename"));
            }

            boolean ownerRole = authentication.getAuthorities().stream()
                    .anyMatch(authority ->
                            "ROLE_OWNER".equals(authority.getAuthority()));

            if (!ownerRole) {
                String uploadActorEmail = authentication.getName()
                        .trim().toLowerCase();
                auditLogService.log(
                        uploadActorEmail,
                        "DOCUMENT_UPLOAD_DENIED",
                        "Upload denied: account is not an owner"
                );
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "error",
                                "Only owner accounts can upload documents"
                        ));
            }

            file.transferTo(destination);

            String ownerEmail = authentication.getName()
                    .trim().toLowerCase();

            Document document = new Document(
                    originalName,
                    category.trim(),
                    ownerEmail,
                    destination.toString(),
                    LocalDateTime.now()
            );

            Document saved = documentRepository.save(document);
            auditLogService.log(
    ownerEmail,
    "DOCUMENT_UPLOAD",
    "Uploaded document: " + saved.getName()
);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Document uploaded successfully",
                    "id", saved.getId(),
                    "name", saved.getName(),
                    "category", saved.getCategory(),
                    "ownerEmail", saved.getOwnerEmail(),
                    "uploadedAt", saved.getUploadedAt().toString()
            ));

        } catch (IOException | RuntimeException e) {
            if (destination != null) {
                try {
                    Files.deleteIfExists(destination);
                } catch (IOException ignored) {
                    // Keep the original upload error response.
                }
            }

            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Unable to upload document"));
        }
    }


    @GetMapping
    public ResponseEntity<?> getMyDocuments(
            Authentication authentication) {

        String userEmail = authentication.getName()
                .trim()
                .toLowerCase();

        var owned = documentRepository.findByOwnerEmail(userEmail);
        var permissions = permissionRepository.findByUserEmail(userEmail);

        var sharedIds = permissions.stream()
                .filter(p -> "READ".equalsIgnoreCase(p.getPermission())
                        || "EDIT".equalsIgnoreCase(p.getPermission()))
                .map(p -> p.getDocumentId())
                .toList();

        var shared = documentRepository.findAllById(sharedIds);

        java.util.Map<Long, Document> all = new java.util.LinkedHashMap<>();
        owned.forEach(doc -> all.put(doc.getId(), doc));
        shared.forEach(doc -> all.put(doc.getId(), doc));

        var result = all.values().stream()
                .map(doc -> Map.of(
                        "id", doc.getId(),
                        "name", doc.getName(),
                        "category", doc.getCategory(),
                        "ownerEmail", doc.getOwnerEmail(),
                        "uploadedAt", doc.getUploadedAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }



@GetMapping("/search")
public ResponseEntity<?> searchDocuments(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String owner,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        Authentication authentication) {

    String userEmail = authentication.getName()
            .trim().toLowerCase();

    java.time.LocalDate start = null;
    java.time.LocalDate end = null;

    try {
        if (startDate != null && !startDate.isBlank()) {
            start = java.time.LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            end = java.time.LocalDate.parse(endDate);
        }
    } catch (java.time.format.DateTimeParseException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Dates must use YYYY-MM-DD format"));
    }

    if (start != null && end != null && start.isAfter(end)) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "startDate must not be after endDate"));
    }

    final java.time.LocalDate searchStart = start;
    final java.time.LocalDate searchEnd = end;

    var results = documentRepository.findAll().stream()
            .filter(doc -> {
                boolean isOwner = doc.getOwnerEmail()
                        .equalsIgnoreCase(userEmail);

                boolean hasRead = permissionRepository
                        .existsByDocumentIdAndUserEmailAndPermission(
                                doc.getId(), userEmail, "READ");

                boolean hasEdit = permissionRepository
                        .existsByDocumentIdAndUserEmailAndPermission(
                                doc.getId(), userEmail, "EDIT");

                return isOwner || hasRead || hasEdit;
            })
            .filter(doc -> name == null || name.isBlank()
                    || doc.getName().toLowerCase()
                    .contains(name.trim().toLowerCase()))
            .filter(doc -> owner == null || owner.isBlank()
                    || doc.getOwnerEmail().equalsIgnoreCase(owner.trim()))
            .filter(doc -> category == null || category.isBlank()
                    || doc.getCategory().equalsIgnoreCase(category.trim()))
            .filter(doc -> {
                java.time.LocalDate uploaded =
                        doc.getUploadedAt().toLocalDate();

                return (searchStart == null
                        || !uploaded.isBefore(searchStart))
                        && (searchEnd == null
                        || !uploaded.isAfter(searchEnd));
            })
            .map(doc -> Map.of(
                    "id", doc.getId(),
                    "name", doc.getName(),
                    "category", doc.getCategory(),
                    "ownerEmail", doc.getOwnerEmail(),
                    "uploadedAt", doc.getUploadedAt().toString()
            ))
            .toList();

    return ResponseEntity.ok(results);
}

@GetMapping("/{documentId}/download")
public ResponseEntity<?> downloadDocument(
        @PathVariable Long documentId,
        Authentication authentication) {

    String userEmail = authentication.getName().trim().toLowerCase();

    Document document = documentRepository.findById(documentId).orElse(null);

    if (document == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Document not found"));
    }

    boolean isOwner = document.getOwnerEmail().equalsIgnoreCase(userEmail);

    boolean hasReadPermission =
        permissionRepository.existsByDocumentIdAndUserEmailAndPermission(
                documentId, userEmail, "READ");

boolean hasEditPermission =
        permissionRepository.existsByDocumentIdAndUserEmailAndPermission(
                documentId, userEmail, "EDIT");

if (!isOwner && !hasReadPermission && !hasEditPermission) {

        auditLogService.log(userEmail, "DOWNLOAD_DENIED",
                "Download denied for document ID: " + documentId);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "You do not have permission to download this document"));
    }

   Path uploadDirectory =
        Paths.get("uploads").toAbsolutePath().normalize();

Path path;

try {
    path = Paths.get(document.getFilePath())
            .toAbsolutePath()
            .normalize();
} catch (RuntimeException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Invalid document path"));
}

if (!path.startsWith(uploadDirectory)) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Invalid document path"));
}

if (!Files.isRegularFile(path)) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "File not found on server"));
}

Resource resource = new FileSystemResource(path);

    auditLogService.log(userEmail, "DOCUMENT_DOWNLOAD",
            "Downloaded document: " + document.getName());

    return ResponseEntity.ok()
            .header("Content-Disposition",
                    "attachment; filename=\"" + path.getFileName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
}

@PutMapping("/{documentId}")
public ResponseEntity<?> editDocument(
        @PathVariable Long documentId,
        @RequestBody Map<String, String> request,
        Authentication authentication) {

    String userEmail = authentication.getName()
            .trim().toLowerCase();

    Document document = documentRepository
            .findById(documentId).orElse(null);

    if (document == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Document not found"));
    }

    boolean isOwner = document.getOwnerEmail()
            .equalsIgnoreCase(userEmail);

    boolean hasEditPermission =
            permissionRepository
                    .existsByDocumentIdAndUserEmailAndPermission(
                            documentId, userEmail, "EDIT");

    if (!isOwner && !hasEditPermission) {
        auditLogService.log(
                userEmail,
                "DOCUMENT_EDIT_DENIED",
                "Edit denied for document ID: " + documentId
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error",
                        "You do not have permission to edit this document"
                ));
    }

    String name = request.get("name");
    String category = request.get("category");

    if (name == null || name.isBlank()
            || category == null || category.isBlank()) {
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error",
                        "Name and category are required"
                ));
    }

    document.setName(name.trim());
    document.setCategory(category.trim());

    Document saved = documentRepository.save(document);

    auditLogService.log(
            userEmail,
            "DOCUMENT_EDIT",
            "Updated document ID: " + documentId
    );

    return ResponseEntity.ok(Map.of(
            "message", "Document updated successfully",
            "id", saved.getId(),
            "name", saved.getName(),
            "category", saved.getCategory()
    ));
}
@Transactional
@DeleteMapping("/{documentId}")
public ResponseEntity<?> deleteDocument(
        @PathVariable Long documentId,
        Authentication authentication) {

    String userEmail = authentication.getName()
            .trim().toLowerCase();

    Document document = documentRepository
            .findById(documentId).orElse(null);

    if (document == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Document not found"));
    }

    if (!document.getOwnerEmail().equalsIgnoreCase(userEmail)) {
        auditLogService.log(
                userEmail,
                "DOCUMENT_DELETE_DENIED",
                "Delete denied for document ID: " + documentId
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error",
                        "Only the document owner can delete this document"
                ));
    }

    try {
        Path filePath = Paths.get(document.getFilePath());

        Files.deleteIfExists(filePath);

        permissionRepository.deleteByDocumentId(documentId);
        documentRepository.delete(document);

        auditLogService.log(
                userEmail,
                "DOCUMENT_DELETE",
                "Deleted document ID: " + documentId
        );

        return ResponseEntity.ok(Map.of(
                "message", "Document deleted successfully",
                "id", documentId
        ));

    } catch (IOException | RuntimeException e) {
        org.slf4j.LoggerFactory
                .getLogger(DocumentController.class)
                .error("Failed to delete document ID {}", documentId, e);
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Unable to delete document"));
    }
}

}

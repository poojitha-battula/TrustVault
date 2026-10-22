
package com.trustvault.backend;

import com.trustvault.backend.entity.Document;
import com.trustvault.backend.repository.AuditLogRepository;
import com.trustvault.backend.repository.DocumentPermissionRepository;
import com.trustvault.backend.repository.DocumentRepository;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.trustvault.backend.entity.DocumentPermission;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private DocumentPermissionRepository permissionRepository;

    private Document ownerDocument;

    @BeforeEach
    void setUp() {
        ownerDocument = mock(Document.class);

        when(ownerDocument.getId()).thenReturn(1L);
        when(ownerDocument.getName()).thenReturn("private.txt");
        when(ownerDocument.getCategory()).thenReturn("private");
        when(ownerDocument.getOwnerEmail()).thenReturn("owner@example.com");
        when(ownerDocument.getFilePath()).thenReturn("uploads/private.txt");
        when(ownerDocument.getUploadedAt()).thenReturn(LocalDateTime.now());

        when(documentRepository.findById(1L))
                .thenReturn(Optional.of(ownerDocument));

        when(permissionRepository
                .existsByDocumentIdAndUserEmailAndPermission(
                        1L, "stranger@example.com", "READ"))
                .thenReturn(false);

        when(permissionRepository
                .existsByDocumentIdAndUserEmailAndPermission(
                        1L, "stranger@example.com", "EDIT"))
                .thenReturn(false);
    }

    @Test
    void viewerCannotAccessAdminAuditLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                .with(user("viewer@example.com").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                .with(user("admin@trustvault.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotAccessAuditLogs() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

   @Test
   void strangerCannotDownloadDocument() throws Exception {
	mockMvc.perform(get("/api/documents/1/download")
            .with(user("stranger@example.com").roles("VIEWER")))
            .andExpect(status().isForbidden());

    ArgumentCaptor<AuditLog> captor =
            ArgumentCaptor.forClass(AuditLog.class);

    verify(auditLogRepository).save(captor.capture());

    assertEquals("DOWNLOAD_DENIED", captor.getValue().getAction());
    assertEquals("stranger@example.com",
            captor.getValue().getActorEmail());
   }
   @Test
   void strangerCannotEditDocument() throws Exception {
	 mockMvc.perform(put("/api/documents/1")
            .with(user("stranger@example.com").roles("VIEWER"))
            .contentType("application/json")
            .content("{\"name\":\"hacked.txt\",\"category\":\"changed\"}"))
            .andExpect(status().isForbidden());

    ArgumentCaptor<AuditLog> captor =
            ArgumentCaptor.forClass(AuditLog.class);

    verify(auditLogRepository).save(captor.capture());

    assertEquals("DOCUMENT_EDIT_DENIED", captor.getValue().getAction());
    assertEquals("stranger@example.com",
            captor.getValue().getActorEmail());
   }
  @Test
  void strangerCannotDeleteDocument() throws Exception {
    mockMvc.perform(delete("/api/documents/1")
            .with(user("stranger@example.com").roles("VIEWER")))
            .andExpect(status().isForbidden());

    ArgumentCaptor<AuditLog> captor =
            ArgumentCaptor.forClass(AuditLog.class);

    verify(auditLogRepository).save(captor.capture());

    assertEquals("DOCUMENT_DELETE_DENIED", captor.getValue().getAction());
    assertEquals("stranger@example.com",
            captor.getValue().getActorEmail());
 }
@Test
void ownerCanRevokePermissionAndAuditIt() throws Exception {
    DocumentPermission permission =
            new DocumentPermission(1L, "viewer@example.com", "READ");

    when(permissionRepository.findByDocumentIdAndUserEmail(
            1L, "viewer@example.com"))
            .thenReturn(Optional.of(permission));

    mockMvc.perform(delete(
            "/api/documents/1/permissions/viewer@example.com")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isOk());

    verify(permissionRepository).delete(permission);

    ArgumentCaptor<AuditLog> captor =
            ArgumentCaptor.forClass(AuditLog.class);

    verify(auditLogRepository).save(captor.capture());

    assertEquals("PERMISSION_REVOKED",
            captor.getValue().getAction());

    assertEquals("owner@example.com",
            captor.getValue().getActorEmail());
}
@Test
void userWithoutPermissionCannotDownloadAfterRevocation() throws Exception {

    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "READ"))
            .thenReturn(false);

    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "EDIT"))
            .thenReturn(false);

    mockMvc.perform(get("/api/documents/1/download")
            .with(user("viewer@example.com").roles("VIEWER")))
            .andExpect(status().isForbidden());

    ArgumentCaptor<AuditLog> captor =
            ArgumentCaptor.forClass(AuditLog.class);

    verify(auditLogRepository).save(captor.capture());

    assertEquals("DOWNLOAD_DENIED",
            captor.getValue().getAction());

    assertEquals("viewer@example.com",
            captor.getValue().getActorEmail());
}
@Test
void uploadRejectsEmptyFile() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}

@Test
void uploadRejectsFileLargerThanFiveMB() throws Exception {
    byte[] largeContent = new byte[5 * 1024 * 1024 + 1];

    MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.txt",
            "text/plain",
            largeContent
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}
@Test
void revokedViewerCannotDownloadDocument() throws Exception {
    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "READ"))
            .thenReturn(false);

    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "EDIT"))
            .thenReturn(false);

    mockMvc.perform(get("/api/documents/1/download")
            .with(user("viewer@example.com").roles("VIEWER")))
            .andExpect(status().isForbidden());

    verify(permissionRepository).existsByDocumentIdAndUserEmailAndPermission(
            1L, "viewer@example.com", "READ");

    verify(permissionRepository).existsByDocumentIdAndUserEmailAndPermission(
            1L, "viewer@example.com", "EDIT");
}
@Test
void revokingPermissionImmediatelyBlocksDownload() throws Exception {
    AtomicBoolean hasReadPermission = new AtomicBoolean(true);

    DocumentPermission permission =
            new DocumentPermission(1L, "viewer@example.com", "READ");

    when(permissionRepository.findByDocumentIdAndUserEmail(
            1L, "viewer@example.com"))
            .thenReturn(Optional.of(permission));

    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "READ"))
            .thenAnswer(invocation -> hasReadPermission.get());

    when(permissionRepository
            .existsByDocumentIdAndUserEmailAndPermission(
                    1L, "viewer@example.com", "EDIT"))
            .thenReturn(false);

    doAnswer(invocation -> {
        hasReadPermission.set(false);
        return null;
    }).when(permissionRepository).delete(permission);

    // Owner revokes the viewer's permission.
    mockMvc.perform(delete(
            "/api/documents/1/permissions/viewer@example.com")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isOk());

    // Viewer cannot download after permission is revoked.
    mockMvc.perform(get("/api/documents/1/download")
            .with(user("viewer@example.com").roles("VIEWER")))
            .andExpect(status().isForbidden());

    verify(permissionRepository).delete(permission);
}
@Test
void uploadRejectsUnsupportedFileExtension() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "program.exe",
            "application/octet-stream",
            "test content".getBytes()
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}

@Test
void uploadRejectsFakePdfContent() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "fake.pdf",
            "application/pdf",
            "this is not a real PDF".getBytes()
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}

@Test
void uploadRejectsFakePngContent() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "fake.png",
            "image/png",
            "this is not a real PNG".getBytes()
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}

@Test
void uploadRejectsBinaryTextFile() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "binary.txt",
            "text/plain",
            new byte[] { 65, 0, 66 }
    );

    mockMvc.perform(multipart("/api/documents/upload")
            .file(file)
            .param("category", "test")
            .with(user("owner@example.com").roles("VIEWER")))
            .andExpect(status().isBadRequest());

    verify(documentRepository, never()).save(any(Document.class));
}



}

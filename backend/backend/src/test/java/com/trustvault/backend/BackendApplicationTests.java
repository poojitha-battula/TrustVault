
package com.trustvault.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.trustvault.backend.repository.AuditLogRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Test
    void viewerCannotAccessAdminAuditLogs() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/admin/audit-logs")
                        .with(user("viewer@example.com").roles("VIEWER"))
        ).andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAuditLogs() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/admin/audit-logs")
                        .with(user("admin@trustvault.com").roles("ADMIN"))
        ).andExpect(status().isOk());
    }

    @Test
    void unauthenticatedUserCannotAccessAuditLogs() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/admin/audit-logs")
        ).andExpect(status().isUnauthorized());
    }
}

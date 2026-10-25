# TrustVault - Security Threat Model

## 1. Overview

TrustVault is a document-sharing application built with Spring Boot and PostgreSQL. This document describes important assets, likely threats, mitigations in the current implementation, and remaining limitations.

## 2. Assets to protect

- User account credentials
- Uploaded documents and their contents
- Document access permissions
- Audit logs
- Database credentials and application configuration

## 3. Threats and mitigations

### Threat 1: Unauthorized document access

**Risk:** A user may try to list, download, edit, or delete a document without permission.

**Mitigation:** Authenticate protected requests and check document ownership or the required per-document permission on the backend. The live test confirmed that a viewer whose permission was revoked received HTTP 403 when attempting a direct download.

### Threat 2: Weak password protection

**Risk:** Exposed or poorly protected passwords may allow account compromise.

**Mitigation:** Store passwords using BCrypt. Keep database credentials in environment configuration and never commit actual passwords or other secrets to Git.

### Threat 3: Privilege escalation

**Risk:** A regular user may attempt to call administrator-only functions.

**Mitigation:** Enforce role-based authorization on protected endpoints, including administrator audit-log access. Keep authorization on the server rather than relying on the web UI.

### Threat 4: Malicious or malformed file uploads

**Risk:** Unexpected file types, oversized uploads, unsafe names, or malformed content may threaten the application.

**Mitigation:** Restrict upload size to 5 MB, allow only the implemented extensions (PDF, TXT, PNG, JPG, JPEG), sanitize the stored filename, and perform basic content checks for supported file types. These checks are not a replacement for malware scanning.

### Threat 5: Unauthorized permission changes

**Risk:** A user may try to grant or revoke permissions on another user's document.

**Mitigation:** Verify ownership and authorization before changing document permissions. Test both successful owner operations and denied operations.

### Threat 6: Sensitive information exposure

**Risk:** Database passwords, document contents, or other sensitive values may leak through source code, logs, or improperly protected endpoints.

**Mitigation:** Keep secrets in environment variables, restrict sensitive endpoints, and avoid logging passwords or document contents. Review logs before sharing them publicly.

### Threat 7: Missing or incomplete audit records

**Risk:** Suspicious or denied activity may be difficult to investigate without reliable records.

**Mitigation:** Record relevant document and permission events, including relevant denied actions, and restrict audit-log access to authorized administrators. Verify event coverage against the actual controller behavior and tests.

### Threat 8: Loss of uploaded files in the deployment environment

**Risk:** Files stored only in a local application directory may disappear after a redeploy, restart, or instance replacement.

**Mitigation / current limitation:** TrustVault stores uploads in the `uploads` directory. The live deployment has returned `404 File not found on server` for database records whose underlying files were missing. Durable persistence has not been confirmed. Configure and verify a persistent disk or external object storage before relying on the live service for durable document storage.

## 4. Verification performed

- Automated backend test run: 16 tests, 0 failures, and 0 errors (latest observed local run).
- Uploaded and listed a document using the owner account.
- Granted `READ` permission to a separate viewer account and verified that the shared document appeared and could be downloaded when its file was present.
- Revoked the permission and verified that the document disappeared from the viewer list and the direct download request returned HTTP 403.
- Fixed the delete transaction issue and verified that the delete operation succeeded in the live application.

These checks describe observed results; they are not a guarantee that every security property is complete.

## 5. Remaining limitations

- Durable uploaded-file storage must be configured and verified for deployment persistence.
- Upload extension and signature/content checks do not prove a file is harmless.
- Audit records in the same database are not tamper-proof against a sufficiently privileged database administrator.
- Automated tests may use mocks and do not replace live integration testing, dependency review, or security testing.

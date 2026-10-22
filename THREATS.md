# TrustVault - Security Threat Model

## 1. Overview

TrustVault is a secure document-sharing application built with Spring Boot and PostgreSQL. This document identifies potential security threats and the measures used to reduce them.

## 2. Assets to Protect

- User account credentials
- Uploaded documents and their contents
- Document access permissions
- Audit logs
- Database credentials and application configuration

## 3. Potential Threats and Mitigations

### Threat 1: Unauthorized Document Access

**Risk:** A user may attempt to access documents without permission.

**Mitigation:** Authenticate users and check document ownership or granted permissions on every protected operation.

### Threat 2: Weak Password Protection

**Risk:** Exposed or poorly protected passwords may allow account compromise.

**Mitigation:** Hash passwords using BCrypt. Never store plaintext passwords or commit secrets to Git.

### Threat 3: Privilege Escalation

**Risk:** A regular user may attempt to access administrator-only functions.

**Mitigation:** Enforce role-based authorization on protected endpoints, including administrative audit-log access.

### Threat 4: Malicious File Uploads

**Risk:** Unexpected files, oversized uploads, or unsafe filenames may threaten the application.

**Mitigation:** Validate uploaded files, limit file size, sanitize filenames, and avoid trusting user-provided file paths.

### Threat 5: Unauthorized Permission Changes

**Risk:** A user may attempt to grant or revoke access to another user's documents.

**Mitigation:** Verify document ownership and authorization before changing document permissions.

### Threat 6: Sensitive Information Exposure

**Risk:** Database passwords or document contents may be exposed through source code, logs, or improper access.

**Mitigation:** Store secrets in environment variables, restrict access to sensitive endpoints, and avoid logging passwords or document contents.

### Threat 7: Missing or Incomplete Audit Records

**Risk:** Suspicious document activity may be difficult to investigate without reliable audit records.

**Mitigation:** Record relevant security events, such as document operations and permission changes, and restrict audit-log access to authorized administrators.

## 4. Verification

- Run automated backend tests.
- Verify unauthenticated requests are rejected.
- Verify non-administrator users cannot access administrator-only endpoints.
- Verify document permissions are enforced.
- Verify upload validation and permission-change authorization.

## 5. Limitations

These mitigations should be verified against the actual implementation. Automated tests do not prove that every security risk has been eliminated. Additional testing is required before production deployment.

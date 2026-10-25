# TrustVault — Secure Document Sharing with an Audit Trail

TrustVault is a Spring Boot and PostgreSQL application for uploading, finding, sharing, and managing documents with document-level permissions and audit logging.

**Live application:** https://trustvault-z11k.onrender.com  
**Source repository:** https://github.com/poojitha-battula/TrustVault

## Features

- User registration and login with passwords stored using BCrypt.
- Role-based authorization for viewers and administrators.
- Document upload, listing, search, download, edit, and owner-only deletion.
- Document-level `READ` and `EDIT` grants that the owner can revoke.
- Authorization checks for document downloads and edits; only the owner can delete a document or change its permissions.
- Search filters for document name, owner, category, and upload date range.
- Audit events for document activity, permission changes, and relevant denied operations.
- Administrator-only audit-log access.
- Upload restrictions: maximum 5 MB; allowed extensions are PDF, TXT, PNG, JPG, and JPEG; file-name handling and basic file-content checks are applied.

## Technology

- Java 21
- Spring Boot
- Spring Security and BCrypt
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven and JUnit / Spring Boot Test

## Repository layout

```text
TrustVault/
├── README.md
├── THREATS.md
├── PROMPTS.md
└── backend/
    └── backend/
        ├── pom.xml
        └── src/
            ├── main/
            └── test/
```

## Run locally

### Prerequisites

- Java 21
- PostgreSQL
- Git

### Configure PostgreSQL

1. Create a PostgreSQL database named `trustvault`.
2. Check `backend/backend/src/main/resources/application.properties` and set the local JDBC URL and username as appropriate.
3. Set `DB_PASSWORD` in the shell where you will run the application. Do not commit a real password to source control.

PowerShell example (prompts securely for the local PostgreSQL password):

```powershell
$secure = Read-Host "Enter your local PostgreSQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password
Remove-Variable secure
```

### Run the application

From the `backend/backend` directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The local application uses port `8080` unless configured otherwise.

### Run tests

From the `backend/backend` directory, set `DB_PASSWORD` in the current PowerShell session first, then run:

```powershell
.\mvnw.cmd test
```

The latest test run reported 16 tests, 0 failures, and 0 errors. Re-run tests after changing the code or configuration.

## API overview

All endpoints other than the explicitly permitted health and authentication routes require authentication.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/health` | Health check |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Authenticate using the application's configured login flow |
| `GET` | `/api/documents` | List documents owned by the signed-in user and documents shared with `READ` or `EDIT` access |
| `POST` | `/api/documents/upload` | Upload a document (`multipart/form-data`) |
| `GET` | `/api/documents/search` | Search by name, owner, category, and date range |
| `GET` | `/api/documents/{documentId}/download` | Download after owner/permission authorization |
| `PUT` | `/api/documents/{documentId}` | Edit metadata with owner or `EDIT` permission |
| `DELETE` | `/api/documents/{documentId}` | Delete a document; owner only |
| `POST` | `/api/documents/{documentId}/permissions` | Grant document-level `READ` or `EDIT` permission as owner |
| `DELETE` | `/api/documents/{documentId}/permissions/{userEmail}` | Revoke a user's document permission as owner |
| `GET` | `/api/admin/audit-logs` | View audit logs; administrator only |

The exact request fields and response schemas should be checked in the corresponding controller methods before integrating another client.

## Security notes and limitations

- Authorization is enforced by the backend; hiding a button or omitting a document from the UI is not a substitute for API authorization.
- Upload validation reduces risk but is not malware scanning and does not guarantee files are harmless.
- Audit logs stored in the same database are not tamper-proof against a sufficiently privileged database administrator.
- **Deployment storage limitation:** uploaded files are stored in the application's `uploads` directory. The live deployment has exhibited missing-file `404` responses after deployment/restart-related changes. Do not assume uploaded files persist across redeploys or restarts unless persistent storage or external object storage has been configured and verified. For reliable production use, configure durable file storage and test restore/persistence behavior.
- Automated tests are useful evidence but do not prove every possible security case. Test the deployed service and review logs before production use.

## Security threat model and AI prompt log

See [`THREATS.md`](THREATS.md) for the threat model and [`PROMPTS.md`](PROMPTS.md) for a retrospective summary of AI-assisted development and testing tasks.

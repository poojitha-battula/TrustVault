# TrustVault - AI Prompt Log

This log was compiled retrospectively from the development and testing work. The entries summarize tasks; they are not claimed to be verbatim transcripts of prompts or responses where the original text was not preserved.

## Entry 1 — 2026-10-19 — Project structure and backend

- **Prompt / task:** Help build TrustVault, a secure document-sharing application with owner, viewer, and admin roles.
- **AI assistance:** Helped plan the Spring Boot backend, document endpoints, database entities, and repository structure.
- **What remained incomplete:** A working endpoint alone does not prove that every request is authorized correctly.
- **Human action:** Implemented and reviewed backend files, configured PostgreSQL, and tested the application locally.
- **Related commit:** `c277cc6` — initial backend and security-test work.

## Entry 2 — 2026-10-20 — Document access control

- **Prompt / task:** Check document download, edit, and delete access for users with and without permission.
- **AI assistance:** Helped review owner checks and per-document `READ` / `EDIT` permission behavior.
- **What remained incomplete:** Authorization must be checked on every relevant request, not only when a document is listed or searched.
- **Human action:** Tested owner and viewer access, an account without permission, and behavior after permission revocation; corrected and re-tested the implementation.

## Entry 3 — 2026-10-20 — Audit trail and admin access

- **Prompt / task:** Verify audit logging for document actions, denied requests, and access to administrator audit logs.
- **AI assistance:** Helped review audit actions and automated tests for authenticated and unauthorized access to the administrator audit endpoint.
- **What remained incomplete:** An audit endpoint must itself be restricted; logging a denied action should not grant access.
- **Human action:** Reviewed authorization tests and checked that relevant denied actions create audit records.

## Entry 4 — 2026-10-21 — Permission revocation regression test

- **Prompt / task:** Review the owner permission-revocation test and verify that the revoked viewer cannot download the document afterward.
- **AI assistance:** Helped inspect test setup and the expected permission-revocation and download-denial behavior.
- **What remained incomplete:** A mock-based test does not by itself prove behavior against a live database or deployed application.
- **Human action:** Updated the test code, ran the Maven test suite, and checked results.

## Entry 5 — 2026-10-21 — Upload validation and file safety

- **Prompt / task:** Review document upload validation and download path handling.
- **AI assistance:** Helped check file-size and file-type validation, filename handling, and ensuring downloaded files remain inside the `uploads` directory.
- **What remained incomplete:** Extension and signature checks are not a substitute for malware scanning or a complete content-security solution.
- **Human action:** Reviewed controller changes and ran the automated tests again.

## Entry 6 — 2026-10-25 — Shared document listing and download authorization

- **Prompt / task:** Fix the document list so a user with `READ` or `EDIT` permission can see the shared document, then verify grant, download, and revoke behavior.
- **AI assistance:** Helped identify that the list endpoint originally returned only owner-owned documents and reviewed the shared-list update.
- **What remained incomplete:** A file record in the database does not guarantee that the uploaded file still exists on the deployed server.
- **Human action:** Deployed the change, verified that the shared document appeared for the viewer, confirmed download when the file was present, then revoked access and verified that the document disappeared and a direct download returned HTTP 403.
- **Related commit:** `d077a6b` — shared documents in viewer list.

## Entry 7 — 2026-10-25 — Delete transaction failure

- **Prompt / task:** Diagnose a live HTTP 500 error when deleting a document.
- **AI assistance:** Helped inspect the Render stack trace, identify the missing transaction around a repository delete operation, and add a transaction to the delete handler.
- **What remained incomplete:** The application also needs durable file storage configured and tested for uploaded files to survive redeployments or restarts.
- **Human action:** Applied the transaction change, ran the automated test suite, deployed it, and verified that deleting a test document succeeded.
- **Related commit:** `1a0558d` — document deletion transaction fix.

## Human review and limitations

- AI suggestions were reviewed and tested during development; this log is a retrospective summary.
- The automated test suite includes mocked components. Live checks were also performed for selected owner/viewer permission flows.
- File-type checks do not guarantee that a file is harmless.
- Audit records stored in the same database are not tamper-proof against a sufficiently privileged database administrator.
- The deployed service has exhibited missing-file responses for documents whose file was no longer present. Durable uploaded-file storage still needs configuration and verification.

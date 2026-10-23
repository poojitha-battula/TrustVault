# TrustVault - AI Prompt Log

This log was compiled retrospectively from the actual development and testing work. Summaries are used where the original prompt or response was not preserved verbatim.

## Entry 1 — 2026-10-19 — Project structure and backend
- **Prompt / task:** Help build TrustVault, a secure document-sharing application with owner, viewer, and admin roles.
- **AI assistance:** Helped plan the Spring Boot backend, document endpoints, database entities, and repository structure.
- **What was incomplete:** A working endpoint alone does not prove that every request is authorized correctly.
- **Human action:** Implemented and reviewed the backend files, configured PostgreSQL, and tested the application locally.
- **Related commit:** `c277cc6` (original backend commit; historical reference).

## Entry 2 — 2026-10-20 — Document access control
- **Prompt / task:** Check document download, edit, and delete access for users with and without permission.
- **AI assistance:** Helped review owner checks and per-document READ/EDIT permission behavior.
- **What was incomplete:** Authorization must be checked on every relevant request, not only when a document is listed or searched.
- **Human action:** Tested owner and viewer access, an account without permission, and behavior after permission revocation; corrected and re-tested the implementation.
- **Related commit:** `4445343` (security hardening and expanded tests).

## Entry 3 — 2026-10-20 — Audit trail and admin access
- **Prompt / task:** Verify audit logging for document actions, denied requests, and access to admin audit logs.
- **AI assistance:** Helped review audit actions and automated tests for authenticated and unauthorized access to the admin audit endpoint.
- **What was incomplete:** An audit endpoint must itself be restricted; logging a denied action should not grant access.
- **Human action:** Added or reviewed authorization tests and checked that relevant denied actions create audit records.
- **Related commit:** `4445343`.

## Entry 4 — 2026-10-21 — Permission revocation regression test
- **Prompt / task:** Review the owner permission-revocation test and verify that the revoked viewer cannot download the document afterward.
- **AI assistance:** Helped inspect the test setup and the expected permission-revocation and download-denial behavior.
- **What was incomplete:** A mock-based test does not by itself prove behavior against a live PostgreSQL database or a deployed application.
- **Human action:** Updated the test code, ran the Maven test suite, and checked the test results.
- **Related commit:** `4445343`.

## Entry 5 — 2026-10-21 — Upload validation and file safety
- **Prompt / task:** Review document upload validation and download path handling.
- **AI assistance:** Helped check file-size and file-type validation, filename handling, and ensuring downloaded files remain inside the uploads directory.
- **What was incomplete:** Extension and file-signature checks are not a substitute for malware scanning or a complete content-security solution.
- **Human action:** Reviewed the controller changes and ran the automated tests again.
- **Related commit:** `4445343`.

## Human review and limitations
- AI-generated suggestions were reviewed and tested during development; this log is a retrospective summary, not a verbatim transcript.
- The automated tests include mocked components. Additional live-database, deployment, and end-to-end checks are still necessary.
- File-type checks do not guarantee that a file is harmless.
- Audit records stored in the same database are not tamper-proof against a sufficiently privileged database administrator.

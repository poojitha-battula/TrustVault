# TrustVault â€” Secure Document Sharing with Audit Trail

## About the Project

TrustVault is a secure document-sharing application developed using Java, Spring Boot, and PostgreSQL. It helps users upload, manage, search, download, and share documents securely. The application uses authentication, role-based access control, document permissions, and audit logging to protect sensitive information.

## Key Features

- User registration and login
- Secure password storage using BCrypt
- Role-based access for users and administrators
- Document upload, listing, search, download, editing, and deletion
- Document-level READ and EDIT permissions
- Granting and revoking document permissions
- Audit logs for document activities and permission changes
- Restricted access to administrator audit logs
- File upload size validation and filename sanitization

## Technologies Used

- **Programming Language:** Java
- **Backend Framework:** Spring Boot
- **Database:** PostgreSQL
- **Data Access:** Spring Data JPA and Hibernate
- **Security:** Spring Security and BCrypt
- **Testing:** JUnit and Spring Boot test support
- **Build Tool:** Maven

## Project Structure

```text
TrustVault/
â”œâ”€â”€ README.md
â”œâ”€â”€ THREATS.md
â”œâ”€â”€ PROMPTS.md
â””â”€â”€ backend/
    â””â”€â”€ backend/
        â”œâ”€â”€ src/
        â”‚   â”œâ”€â”€ main/
        â”‚   â””â”€â”€ test/
        â””â”€â”€ pom.xml
```

## Prerequisites

- Java 21
- PostgreSQL
- Git

## Database Configuration

1. Create a PostgreSQL database named `trustvault`.
2. Configure the database URL and username in `application.properties`.
3. Set the `DB_PASSWORD` environment variable to your local PostgreSQL password.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/trustvault
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
server.port=8080
```

Do not commit passwords or other secrets to GitHub.

## Run the Application

Open PowerShell in the `backend/backend` directory.

Set your database password for the current PowerShell session:

```powershell
$env:DB_PASSWORD = "your-local-postgres-password"
```

Replace the placeholder with your actual local password. Do not share it.

Run automated tests:

```powershell
.\mvnw.cmd test
```

Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The application is configured to run on port `8080`.

## Security

- Authentication is required for protected endpoints.
- Role-based authorization protects administrator functions.
- Document permissions control access to shared documents.
- Passwords are hashed rather than stored as plaintext.
- Audit logs help track relevant document and permission activities.
- Environment variables are used to keep database passwords out of application configuration.

## Testing

Run the backend test suite:

```powershell
.\mvnw.cmd test
```

The existing automated tests check authorization for administrative audit logs, including unauthenticated and forbidden requests.

## Future Improvements

- Build a user-friendly frontend.
- Add more automated tests for document permissions and file validation.
- Deploy using HTTPS and secure production configuration.
- Improve monitoring and audit-log reporting.

## Project Status

The backend implementation and automated security tests have been developed. Additional features and deployment requirements should be verified against the original hiring challenge specification.

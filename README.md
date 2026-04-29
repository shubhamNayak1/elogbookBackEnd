# PharmaTrack eLogbook — Backend

Spring Boot 3 + PostgreSQL backend implementing the API contract described in
`PharmaTrack eLogbook — Backend Workflow Specification`.

## Stack
- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL (JSONB for column options, entry values, audit diffs)
- Flyway for migrations
- JJWT for stateless JWT auth
- Lombok

## Layout
```
src/main/java/com/pharmatrack/elogbook
  ├── ElogbookApplication.java
  ├── config/                — Spring Security + CORS
  ├── security/              — JWT service, auth filter, current-user accessor
  ├── domain/
  │     ├── enums/           — UserRole, ColumnType, LogbookStatus, EntryStatus, AuditAction, EntityType
  │     ├── entity/          — JPA entities mirroring `types.ts`
  │     └── repository/      — Spring Data repositories
  ├── api/
  │     ├── controller/      — REST controllers, one per /api/* tag
  │     └── dto/             — request / response records, Mappers
  ├── service/               — AuthService, UserService, LogbookService, EntryService,
  │                            AuditQueryService, ReportService, StateService, CsvExportService
  ├── audit/                 — AuditService (append-only writer, propagation = MANDATORY)
  └── exception/             — ApiException hierarchy + GlobalExceptionHandler
src/main/resources
  ├── application.yml
  └── db/migration/V1__init.sql
```

## Running

### 1. Postgres
```bash
docker run --name pharmatrack-pg -d \
  -e POSTGRES_DB=pharmatrack \
  -e POSTGRES_USER=pharmatrack \
  -e POSTGRES_PASSWORD=pharmatrack \
  -p 5432:5432 postgres:16
```

### 2. Build & run
```bash
cd backend
./mvnw spring-boot:run
# or
./mvnw -DskipTests package && java -jar target/elogbook-0.1.0.jar
```

The first boot runs `V1__init.sql`, which seeds an `admin` user.

### 3. Environment
| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/pharmatrack` |
| `DB_USER` | `pharmatrack` |
| `DB_PASSWORD` | `pharmatrack` |
| `JWT_SECRET` | dev-only fallback — **set a real 32+ byte secret in any non-local environment** |
| `JWT_EXP_MINUTES` | `480` |
| `CORS_ORIGINS` | `http://localhost:5173,http://localhost:3000` |

## Compliance guarantees baked in
- Every mutating endpoint requires a non-empty `reason` (validated in DTO).
- `AuditService` runs with `Propagation.MANDATORY`, so an audit row only commits if
  the same outer transaction commits the data change.
- `audit_records` has a Postgres trigger that rejects UPDATE / DELETE — **append-only
  even for superusers**.
- Audit and report queries cap their date range at 31 days (`app.audit.range-max-days`).
- Entries with `status = SIGNED` reject PUT and only accept DELETE-with-reason.
- ACTIVE templates reject column removals or type changes unless `migrate=true` is
  supplied in the body.
- `system-managed` columns (e.g. `Time`) are auto-injected at `displayOrder=-1` and
  rejected on update.
- Non-admin callers are filtered to their own `userId` for entries, audit, and reports.

## Endpoint map
Mirrors the OpenAPI in the spec:

```
POST   /api/auth/login            (public)
POST   /api/auth/logout
GET    /api/state
GET    /api/users/public          (public)
GET    /api/users                 (admin)
POST   /api/users                 (admin)
PUT    /api/users/{id}            (admin)
DELETE /api/users/{id}            (admin)
GET    /api/logbooks?status=
GET    /api/logbooks/{id}
POST   /api/logbooks              (admin)
PUT    /api/logbooks/{id}         (admin)
DELETE /api/logbooks/{id}         (admin)
GET    /api/entries?logbookId&startDate&endDate
POST   /api/entries
PUT    /api/entries/{id}
DELETE /api/entries/{id}
GET    /api/audit?startDate&endDate&search&action
GET    /api/audit/{id}
POST   /api/audit/export          → text/csv
GET    /api/reports/entries?logbookId&startDate&endDate
POST   /api/reports/export        → text/csv
```

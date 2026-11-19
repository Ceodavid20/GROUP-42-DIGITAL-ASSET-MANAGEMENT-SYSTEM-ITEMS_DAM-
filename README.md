

## 1. What was built

Per Chapters 3–4 of the report:

| Area (report ref) | Implementation |
|---|---|
| Asset registration + unique tag (FR-02, FR-11) | `POST /api/v1/assets` — mandatory tag (unique, 409 on duplicate), QR-ready `qrCodeData`; validation errors 400/422 |
| Search / filter (FR-03) | `GET /api/v1/assets?query=&categoryId=&locationId=&unitName=&status=&serialNumber=` — paged |
| Assign + return (FR-04, §4.2.3/§4.2.5) | `POST /api/v1/assignments`, `POST /api/v1/assignments/{id}/return` — single active assignment, atomic state change ASSIGNED↔AVAILABLE; double-issue rejected with no partial transaction |
| Controlled transfer (FR-05, Fig. 3.4) | `POST /api/v1/transfers` (PENDING, location unchanged) → `POST /api/v1/transfers/{id}/decision` (approver only; APPROVED moves location in one transaction; REJECTED keeps it). Idempotent re-decision |
| Maintenance (FR-06) | `POST /api/v1/maintenance`, `PATCH /api/v1/maintenance/{id}` — OPEN blocks assignment/disposal; RESOLVED returns asset to AVAILABLE/ASSIGNED |
| Retirement / disposal (FR-07) | `POST /api/v1/assets/{id}/retire` (approval + reason), `POST /api/v1/assets/{id}/dispose` (RETIRED-only + sanitization evidence note) |
| Reports + exceptions (FR-08, FR-12) | `GET /api/v1/reports/summary`, `/summary/unit`, `/exceptions/unverified`, `/exceptions/missing-metadata` — every report states extraction date + filters |
| Audit trail (FR-09) | Append-only `audit_events` (actor/action/time/entity + client IP); written on every state change with the authenticated actor captured |
| Master data (FR-10) | `/api/v1/reference/categories`, `/api/v1/reference/locations` |
| Restricted attachments (Table 3.3) | `POST /api/v1/attachments` (multipart: assetId, file, accessLabel) + download; ADMIN/ASSET_OFFICER/AUDITOR only |
| Auth + roles (FR-01, Table 3.2) | Spring Security + signed JWT; server-enforced per endpoint; BCrypt passwords; 401/403 JSON errors |
| States (Fig. 3.3) | `REGISTERED → AVAILABLE → ASSIGNED → AVAILABLE → UNDER_MAINTENANCE → RETIRED → DISPOSED`, enforced in services |

### Roles (Table 3.2)

| Capability | ADMIN | ASSET_OFFICER | UNIT_CUSTODIAN | TECHNICIAN | AUDITOR |
|---|---|---|---|---|---|
| Manage users | Yes | — | — | — | — |
| Register / verify assets | Config | Yes | — | — | Read |
| Request movement | — | Yes | Own unit | — | Read |
| Approve movement | — | Yes | — | — | Read |
| Update maintenance | — | View | Request | Yes | Read |
| Reports | System | Yes | Own unit | Assigned | Yes |
| Retire / dispose | — | Request/approve | — | Recommend | Read |
| Audit log | Security scope | Yes | — | Limited | Yes |

### Acceptance evidence (Table 4.1, verified 2026-11-11 against live API)

| ID | Scenario | Observed result |
|---|---|---|
| TC-01 | Create asset, valid data | 201 + Location; status REGISTERED |
| TC-02 | Duplicate tag | 409; no second row |
| TC-03 | Issue AVAILABLE | 201; single assignment; ASSIGNED |
| TC-04 | Issue already-assigned | 422; state unchanged (no partial transaction) |
| TC-05 | Approve transfer | Location moved atomically; history consistent |
| TC-06 | Reject transfer | Location unchanged; decision recorded |
| TC-07 | Open maintenance | Ticket linked; asset UNDER_MAINTENANCE (blocks assign/dispose) |
| TC-08 | Unauthorized mutation | 403 + 401; no data alteration |
| TC-09 | Report counts vs seeded data | Matches (6 seed assets, one per lifecycle state) |
| TC-10 | Restore test backup | Not executed (backup drill required before production use) |
| TC-11 | QR/tag lookup | Tag query resolves to the authorized record with `qrCodeData` |
| TC-12 | Exception reports | Unverified + missing-metadata lists returned with extraction date |

---

## 2. Run it

### Backend (Java 21)

Default (PostgreSQL):

```bash
cd backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
~/tools/apache-maven-3.9.6/bin/mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

- API: `http://localhost:8080/api/v1`
- Swagger: `http://localhost:8080/api/v1/swagger-ui.html`
- Dev profile seeds fictitious DEMO users, categories, locations, and six assets (one per lifecycle state)

### PostgreSQL (persistent database)

The `postgres` Spring profile points at a local PostgreSQL
(`items_dam_db`, role `items_admin`). One-time setup with Postgres.app:

```bash
export PATH="/Applications/Postgres.app/Contents/Versions/latest/bin:$PATH"
psql -h localhost -U mac1 -d postgres -c \
  "CREATE ROLE items_admin WITH LOGIN PASSWORD 'UiDamSecure2024!';"
createdb -h localhost -U mac1 -O items_admin items_dam_db
```

Then run the backend against it (schema auto-created on first boot;
seeds load only when the `users` table is empty):

```bash
cd backend
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
~/tools/apache-maven-3.9.6/bin/mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

Configuration lives in `backend/src/main/resources/application-postgres.yml`
(URL, user, pool sizes). The `prod` profile (`application-prod.yml`) reads the
same settings from `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` env vars for
Docker/managed hosting (see `docker-compose.yml`). Your Angular
`api.service.ts` needs no changes — it calls `/api/v1`, which the dev proxy
(or nginx in Docker) forwards regardless of which database the backend uses.

### Frontend (Angular 18)

```bash
cd frontend
npm install
npm start   # serves on :4200 with proxy to :8080
```

### Demo accounts (fictitious)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `officer` | `officer123` | ROLE_ASSET_OFFICER |
| `custodian` | `custodian123` | ROLE_UNIT_CUSTODIAN |
| `technician` | `technician123` | ROLE_MAINTENANCE_TECHNICIAN |
| `auditor` | `auditor123` | ROLE_AUDITOR |

### Docker

`docker-compose.yml` provides Postgres + MinIO + backend + frontend for production-like hosting.
Before production use: set `JWT_SECRET`, run the backup/restore drill (TC-10), complete
Appendix D approvals, and replace all DEMO seeds.

---

## 3. Layout

```
backend/src/main/java/ng/edu/ui/items/dam/
  UiItemsDamApplication.java
  config/         # SecurityConfig, OpenApiConfig, DataSeeder (DEMO)
  controller/     # auth, assets, assignments, transfers, maintenance,
                  # reports, audit, reference, attachments, admin users
  dto/            # request/response payloads (no entity leakage)
  entity/         # Asset, Assignment, Transfer, Maintenance, AuditEvent,
                  # Category, Location, Attachment, User, Role
  enums/          # AssetStatus, TransferDecision, MaintenanceStatus, ERole, AuditAction
  exception/      # domain exceptions + consistent JSON error contract
  repository/     # Spring Data JPA + Specifications
  security/       # JWT filter, provider, UserDetails
  service/        # Asset, Assignment, Transfer, Maintenance, Retirement,
                  # Report, ReferenceData, User, Attachment, Audit
frontend/src/app/
  app.component.ts / app.routes.ts / app.config.ts
  core/           # models, ApiService, AuthService, interceptor, guards
  features/       # auth, dashboard, assets, transfers, maintenance, reports, audit
```

## 4. Honest limitations (per Chapter 5)

- Design-stage prototype; no ITeMS fieldwork, production data, or user-testing results.
- TC-10 (backup restore) not executed; required before any production claim.
- Software-license records, RFID readers, AD integration, and org-wide rollout are
  explicitly out of scope (later phases).

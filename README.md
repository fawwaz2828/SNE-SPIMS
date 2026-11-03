<<<<<<< HEAD
# SNE-SPIMS — Spring Boot 3

Student Progress & Information Management System for a special-needs school
(branded in the UI as **Luna Bridge**). Re-platformed from an original
Servlet/JSP app to **Spring Boot 3 + Thymeleaf + MyBatis + MySQL**.

## Stack
- Spring Boot 3.3.x, Java 17 (build/run on JDK 17 or 21 LTS)
- Thymeleaf + Layout Dialect + Bootstrap 5 (CDN)
- MyBatis 3.5 (XML mappers) over MySQL 8, HikariCP pool
- Spring Security form login (BCrypt; existing `$2a$` hashes verify natively, via jBCrypt helper)

## Prerequisites
1. **JDK 17 or 21.** (A newer JDK may be on PATH, but Spring Boot 3.3 is only
   certified on 17/21 — point `JAVA_HOME` at one of those.)
2. **Maven 3.9+** — use IntelliJ's bundled Maven or install it.
3. **MySQL 8** running locally.

## Database setup
```sql
-- fresh install (schema.sql DROPS and recreates the sne_spims database):
SOURCE src/main/resources/db/schema.sql;
SOURCE src/main/resources/db/seed.sql;
SOURCE src/main/resources/db/seed_parents.sql;        -- parent links
-- optional: more demo caseload data
SOURCE src/main/resources/db/seed_extra_caseload.sql;
```
> ⚠️ `schema.sql` runs `DROP DATABASE sne_spims` — it wipes all data. Use it only
> for a clean rebuild, never against a live database.

Adjust credentials in `src/main/resources/db.properties`.

## Run
```bash
mvn spring-boot:run
# then open http://localhost:8080
```

## Default logins (from seed.sql)
| Role    | Username | Password   | Notes                                   |
|---------|----------|------------|-----------------------------------------|
| Admin   | admin    | admin123   | System Admin                            |
| Teacher | james    | teacher123 | Class A teacher                         |
| Teacher | laura    | teacher123 | Class B teacher                         |
| Parent  | robert   | parent123  | Father of Ryan                          |
| Parent  | linda    | parent123  | Mother of Daisy, guardian of Jacob (2)  |

## Modules (ported to Spring MVC)
- **Auth** — form login, role-based access (ADMIN / TEACHER / PARENT).
- **Dashboard** — role-aware; live analytics and operational widgets (`/dashboard`).
- **Students** — roster, profiles, teacher/parent assignment (`/students`).
- **Daily reports** — rubric scoring, draft→publish, parent acknowledgement (`/reports`).
- **IEP** — programs and measurable goals with progress history (`/iep`).
- **Behaviour incidents** — severity, parent-visibility rules (`/incidents`).
- **Therapy** — scheduling with student/teacher/room conflict checks (`/therapy`).
- **Messaging** — parent–teacher chat anchored to a student (`/messages`).
- **Calendar** — therapy + goal dates + school events, role-scoped (`/calendar`).
- **Notifications** — inbox + bell badge; 07:00 therapy-reminder cron (`/notifications`).
- **Announcements** — admin, role-targeted (`/announcements`).
- **Users / Account** — admin user management; self-service password change.

## Architecture notes
- Layered: `web` (controllers) → `service` (business rules, access control,
  transactions) → `dao`/`dao.impl` → `util.MyBatis` → MySQL.
- The MyBatis data layer keeps its own HikariCP pool from `db.properties`;
  Spring's `DataSourceAutoConfiguration` is excluded.
- Multi-step writes are made atomic via `MyBatis.tx(...)` (see
  `com.school.util.MyBatis`).
- Access is decided centrally in `com.school.service.AccessControl`.

## Not yet ported
The schema also contains tables for features that still live only in the
original Servlet app and have **no Spring MVC code yet**:
attachments (file upload), attendance, medical records / medications,
and the IEP goal-bank templates. Server-side PDF export is likewise not yet
implemented in this build.

## Tests
```bash
mvn test    # JUnit 5 + Mockito (access control + report service)
```
=======
# SNE-SPIMS
>>>>>>> d671dfb4807aaaf701b267ac19157bc3768486eb

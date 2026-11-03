/* SNE-SPIMS technical document generator (docx-js). */
const fs = require("fs");
const path = require("path");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  ImageRun, Header, Footer, AlignmentType, LevelFormat, TabStopType,
  TabStopPosition, TableOfContents, HeadingLevel, BorderStyle, WidthType,
  ShadingType, PageNumber, PageBreak,
} = require("docx");

const DIA = path.join(__dirname, "diagrams");
const CONTENT_W = 9360; // US Letter, 1" margins
const INK = "1F2933";
const BLUE = "2E6FB0";
const CODE_BG = "F4F6F8";
const HEAD_BG = "D5E8F0";
const ZEBRA = "EEF3F8";

/* ---------- helpers ---------- */
const H1 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun(t)] });
const H2 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_2, children: [new TextRun(t)] });
const H3 = (t) => new Paragraph({ heading: HeadingLevel.HEADING_3, children: [new TextRun(t)] });

function P(text, opts = {}) {
  const runs = Array.isArray(text) ? text : [new TextRun(text)];
  return new Paragraph({ children: runs, spacing: { after: 140, line: 276 }, ...opts });
}
const bold = (t) => new TextRun({ text: t, bold: true });
const it = (t) => new TextRun({ text: t, italics: true });
const mono = (t) => new TextRun({ text: t, font: "Consolas", size: 19 });

function bullet(text, level = 0) {
  const runs = Array.isArray(text) ? text : [new TextRun(text)];
  return new Paragraph({ numbering: { reference: "bullets", level }, children: runs, spacing: { after: 60 } });
}
function numItem(text) {
  const runs = Array.isArray(text) ? text : [new TextRun(text)];
  return new Paragraph({ numbering: { reference: "nums", level: 0 }, children: runs, spacing: { after: 60 } });
}

function code(lines, caption) {
  const out = [];
  lines.forEach((ln, i) => {
    out.push(new Paragraph({
      shading: { type: ShadingType.CLEAR, fill: CODE_BG },
      spacing: { after: 0, before: 0, line: 240 },
      border: { left: { style: BorderStyle.SINGLE, size: 18, color: BLUE, space: 6 } },
      indent: { left: 120 },
      children: [new TextRun({ text: ln === "" ? " " : ln, font: "Consolas", size: 17, color: "243B53" })],
    }));
  });
  if (caption) {
    out.push(new Paragraph({
      spacing: { before: 60, after: 160 },
      children: [new TextRun({ text: caption, italics: true, size: 17, color: "627D98" })],
    }));
  } else {
    out.push(new Paragraph({ spacing: { after: 120 }, children: [] }));
  }
  return out;
}

function cell(text, { headerCell = false, w, bgFill, alignLeft = true } = {}) {
  const runs = Array.isArray(text) ? text : [new TextRun({ text: String(text), bold: headerCell })];
  return new TableCell({
    width: { size: w, type: WidthType.DXA },
    shading: bgFill ? { type: ShadingType.CLEAR, fill: bgFill } : undefined,
    margins: { top: 60, bottom: 60, left: 120, right: 120 },
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1, color: "C7D0DA" },
      bottom: { style: BorderStyle.SINGLE, size: 1, color: "C7D0DA" },
      left: { style: BorderStyle.SINGLE, size: 1, color: "C7D0DA" },
      right: { style: BorderStyle.SINGLE, size: 1, color: "C7D0DA" },
    },
    children: [new Paragraph({ alignment: alignLeft ? AlignmentType.LEFT : AlignmentType.CENTER, children: runs })],
  });
}

function table(headers, rows, widths) {
  const tRows = [];
  tRows.push(new TableRow({
    tableHeader: true,
    children: headers.map((h, i) => cell(h, { headerCell: true, w: widths[i], bgFill: HEAD_BG })),
  }));
  rows.forEach((r, ri) => {
    tRows.push(new TableRow({
      children: r.map((c, i) => {
        if (c && c.__runs) return cell(c.__runs, { w: widths[i], bgFill: ri % 2 ? ZEBRA : undefined });
        return cell(String(c), { w: widths[i], bgFill: ri % 2 ? ZEBRA : undefined });
      }),
    }));
  });
  return new Table({ width: { size: CONTENT_W, type: WidthType.DXA }, columnWidths: widths, rows: tRows });
}
const runsCell = (runs) => ({ __runs: runs });

function pngSize(file) {
  const b = fs.readFileSync(file);
  return { w: b.readUInt32BE(16), h: b.readUInt32BE(20) };
}
function img(file, displayW, title) {
  const full = path.join(DIA, file);
  const { w, h } = pngSize(full);
  const width = displayW;
  const height = Math.round((h / w) * width);
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 120, after: 80 },
    children: [new ImageRun({
      type: "png", data: fs.readFileSync(full),
      transformation: { width, height },
      altText: { title, description: title, name: title },
    })],
  });
}
const caption = (t) => new Paragraph({
  alignment: AlignmentType.CENTER, spacing: { after: 200 },
  children: [new TextRun({ text: t, italics: true, size: 18, color: "627D98" })],
});
const PB = () => new Paragraph({ children: [new PageBreak()] });

/* ============================================================ CONTENT */
const body = [];

/* ---------- Title page ---------- */
body.push(
  new Paragraph({ spacing: { before: 2600 }, alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "SNE-SPIMS", bold: true, size: 72, color: BLUE })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 },
    children: [new TextRun({ text: "Special-Needs Education", bold: true, size: 34, color: INK })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 400 },
    children: [new TextRun({ text: "Student Progress & Information Management System", bold: true, size: 34, color: INK })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 120 },
    children: [new TextRun({ text: "Technical Design & Implementation Document", size: 26, color: "486581" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 1200 },
    children: [new TextRun({ text: "Spring Boot 3 · Thymeleaf · MyBatis · MySQL 8", size: 22, italics: true, color: "627D98" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER,
    children: [new TextRun({ text: "Application brand: “Luna Bridge”", size: 20, color: "627D98" })] }),
  new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 1400 },
    children: [new TextRun({ text: "Version 1.0.0", size: 22, color: INK })] }),
  PB(),
);

/* ---------- TOC ---------- */
body.push(
  new Paragraph({ heading: HeadingLevel.HEADING_1, children: [new TextRun("Table of Contents")] }),
  new TableOfContents("Table of Contents", { hyperlink: true, headingStyleRange: "1-3" }),
  PB(),
);

/* ================= 1. INTRODUCTION ================= */
body.push(H1("1. Introduction"));

body.push(H2("1.1 Background"));
body.push(P([
  new TextRun("SNE-SPIMS (Special-Needs Education — Student Progress & Information Management System), branded in the interface as "),
  bold("Luna Bridge"),
  new TextRun(", is a web application that helps a special-needs school handle the day-to-day record-keeping and communication around its students. Special-needs education is very individual: each child follows an Individualised Education Program (IEP), receives one or more kinds of therapy, and is supported by both teachers and parents. The information around a single child (progress notes, behaviour incidents, therapy schedules, medical alerts, messages) is sensitive, and it is shared between several people who each have a different right to see it."),
]));
body.push(P("The system was re-platformed from an original Java Servlet/JSP application onto Spring Boot 3, keeping the existing MyBatis data layer and the BCrypt-hashed user accounts rather than rewriting them. This document describes the resulting architecture, the design decisions behind it, and how the Spring MVC system is implemented."));

body.push(H2("1.2 Problem Statement"));
body.push(P("Paper-based or spreadsheet-based tracking of special-needs students fails on three fronts: (1) information is siloed, so a parent cannot easily see their child’s published progress; (2) there is no enforced boundary preventing one teacher or parent from seeing another family’s sensitive records; and (3) multi-step records (a report and its rubric scores, a goal and its progress history) can be left half-written when something fails mid-save. SNE-SPIMS addresses all three with role-scoped access, a clean service layer, and atomic database transactions."));

body.push(H2("1.3 Objectives"));
body.push(bullet("Provide a single, role-aware system for student records, IEPs, daily reports, behaviour incidents, therapy scheduling and parent–teacher messaging."));
body.push(bullet("Enforce strict, centrally-defined access rules so each role sees only the students it is entitled to."));
body.push(bullet("Guarantee data integrity for every multi-step write through database transactions."));
body.push(bullet("Keep parents informed automatically through an in-app notification system and scheduled reminders."));
body.push(bullet("Offer a warm, accessible user interface tailored to each role."));

body.push(H2("1.4 Scope"));
body.push(P([bold("In scope: "), new TextRun("user & account management, student profiles, teacher/parent assignment, IEP programs and measurable goals with progress history, daily progress reports with rubric scoring and parent acknowledgement, behaviour incident logging, therapy session scheduling, a unified calendar, parent–teacher messaging, role-aware dashboards and analytics, and an event-driven notification inbox.")]));
body.push(P([bold("Not implemented in the codebase: "), new TextRun("the database schema also defines tables for file attachments, attendance, medical/medication management and the IEP goal-bank, but no controller, service, DAO, or screen in the current code uses them. They are listed in the data model only because they exist in schema.sql; this document describes them as unbuilt, matching the code exactly.")]));

body.push(H2("1.5 Actors & Stakeholders"));
body.push(table(
  ["Actor", "Description", "Primary responsibilities"],
  [
    ["Admin", "School administrator", "Manage users, assign teachers/parents to students, publish announcements, view school-wide analytics."],
    ["Teacher", "Class / subject teacher", "Author daily reports, log incidents, manage IEP goals & progress, schedule therapy, message parents."],
    ["Parent", "Parent or guardian", "View their child’s published reports and progress, acknowledge reports, message teachers, read notifications."],
  ],
  [1700, 3260, 4400],
));
body.push(P("Two clarifications. First, the system has exactly these three roles; there is no separate “therapist” role. A therapy session is run by a teacher (recorded as therapist_id on the session), so wherever this document says “the teacher running the session”, that is the person other systems might call a therapist. Second, the 07:00 therapy reminder is not triggered by a person but by a background timer, so it is described in the text rather than drawn as an actor."));

body.push(H2("1.6 Technology Stack"));
body.push(table(
  ["Layer", "Technology"],
  [
    ["Runtime / framework", "Spring Boot 3.3.5, Java 17 (LTS)"],
    ["Web / view", "Spring MVC, Thymeleaf + Layout Dialect, Bootstrap 5"],
    ["Security", "Spring Security (form login), jBCrypt ($2a$ hashes), CSRF protection"],
    ["Persistence", "MyBatis 3.5 (XML mappers), MySQL 8, HikariCP connection pool"],
    ["Testing", "JUnit 5, Mockito, AssertJ, Spring Security Test"],
  ],
  [3000, 6360],
));
body.push(P([it("Note: "), new TextRun("server-side PDF export is a planned feature and is not part of this build, so no PDF library is included in the stack above.")]));

body.push(H2("1.7 System Overview"));
body.push(P("The diagram below groups the main use cases under the three actors. Some of them (dashboard, messaging, calendar, notifications) are shared by teachers and parents, but each one is role-scoped inside, so what a teacher sees and what a parent sees are not the same. The 07:00 therapy reminder runs on a background timer rather than a person, so it is described in the text instead of being drawn as an actor."));
body.push(img("uml_usecase.png", 600, "Use case diagram"));
body.push(caption("Figure 1. SNE-SPIMS use case diagram."));
body.push(PB());

/* ================= 2. DESIGN ================= */
body.push(H1("2. Design"));

body.push(H2("2.1 Architectural Overview"));
body.push(P("SNE-SPIMS follows a classic layered architecture. A request from the browser passes through the Spring Security filter chain, is handled by a thin Spring MVC controller, delegates business rules to a service, and reaches the database only through a DAO and the shared MyBatis helper. Each layer depends only on the layer beneath it; no controller talks to a DAO directly, and no business rule leaks into a Thymeleaf template."));
body.push(img("uml_architecture.png", 480, "Layered architecture"));
body.push(caption("Figure 2. Layered architecture and the flow of a request."));
body.push(P([
  new TextRun("The "), bold("MyBatis bridge"),
  new TextRun(" is worth calling out: instead of adopting Spring Data, the project keeps its existing MyBatis mapper XML and its own HikariCP pool, and exposes the hand-wired service objects to Spring as beans through a single "),
  mono("LegacyServicesConfig"),
  new TextRun(" configuration class. This preserved the existing data layer while gaining Spring MVC, Spring Security and dependency injection."),
]));

body.push(H2("2.2 Layered Responsibilities"));
body.push(table(
  ["Layer", "Package", "Responsibility"],
  [
    ["Presentation", "com.school.web", "HTTP routing, form binding, role guards, flash messages, model assembly for views."],
    ["Security", "com.school.security", "Authentication (form login), authority mapping, URL-level authorisation, CSRF."],
    ["Service", "com.school.service / notify / analytics / calendar", "Business rules, validation, access-control enforcement, transactions, event publishing."],
    ["Persistence", "com.school.dao(.impl)", "DAO interfaces + implementations mapping to MyBatis statements."],
    ["Infrastructure", "com.school.util", "MyBatis helper (incl. transactions), password hashing, lookups, exceptions."],
    ["Domain", "com.school.model", "Plain data objects mirroring the database entities."],
  ],
  [1500, 3260, 4600],
));

body.push(H2("2.3 Package Structure"));
body.push(...code([
  "com.school",
  "├── SneSpimsApplication        (Spring Boot entry point)",
  "├── config        LegacyServicesConfig  (bridge to hand-wired services)",
  "├── web           *Controller, GlobalExceptionHandler, CurrentUserAdvice",
  "├── security      SecurityConfig, AppUserDetails(Service), SecurityUtils",
  "├── service       AccessControl, *Service, Services (locator)",
  "├── dao           *Dao  +  dao.impl  *DaoImpl",
  "├── model         User, Student, IepProgram, DailyReport, ...",
  "├── notify        NotificationService, event/*, NotificationEventListener",
  "├── analytics     AnalyticsService/Repository, DashboardData, TrendChart",
  "├── calendar      CalendarService/Repository, CalendarEvent",
  "└── util          MyBatis, Db, Passwords, Lookups, *Exception",
], "Listing 1. Application package layout."));

body.push(H2("2.4 Domain Model"));
body.push(P("The domain model centres on two anchor entities — User and Student — joined by two assignment tables that encode the all-important access relationships: teacher_student (caseload) and parent_student (guardianship). Every other record (IEP, report, incident, therapy session, message) hangs off a student, which is why access can be decided uniformly from a single studentId."));
body.push(img("uml_class.png", 600, "Domain class diagram"));
body.push(caption("Figure 3. Core domain entities and their relationships."));

body.push(H2("2.5 Database Design"));
body.push(P("The schema comprises sixteen interrelated core tables (plus additional tables reserved for the not-yet-ported legacy features). Referential integrity is enforced with foreign keys; cascade deletes remove dependent rows when a parent record is removed, and uniqueness constraints prevent duplicates (one report per student per day, one IEP per student per academic year, one acknowledgement per parent per report)."));
body.push(P([bold("Core tables")]));
body.push(table(
  ["Table", "Purpose", "Key constraints"],
  [
    ["users", "Accounts for all roles", "unique username, unique email; role ENUM"],
    ["students", "Student profiles", "unique student_code; disability/support ENUMs"],
    ["teacher_student", "Teacher caseload (M:N)", "unique (teacher_id, student_id); FK cascade"],
    ["parent_student", "Guardianship (M:N)", "unique (parent_id, student_id); relationship ENUM"],
    ["iep_programs", "IEP per student/year", "unique (student_id, academic_year)"],
    ["iep_goals", "Measurable goals", "FK to iep; progress_pct CHECK 0..100"],
    ["iep_goal_progress_history", "Progress snapshots", "unique (goal_id, recorded_on)"],
    ["daily_reports", "Daily progress reports", "unique (student_id, report_date); status ENUM"],
    ["report_rubrics", "Per-criterion ratings", "unique (report_id, criterion); rating ENUM"],
    ["report_acknowledgements", "Parent read-receipts", "unique (report_id, parent_id)"],
    ["behavior_incidents", "Behaviour logging", "severity ENUM; parent_visible flag"],
    ["therapy_sessions", "Therapy scheduling", "CHECK end_time > start_time; status ENUM"],
    ["messages", "Direct messaging", "FK sender/recipient; optional student context"],
    ["announcements", "Role-targeted notices", "target_role ENUM; active flag"],
    ["notifications", "Per-user inbox", "type ENUM; is_read / is_archived flags"],
    ["school_calendar", "School-wide events", "indexed by event_date"],
  ],
  [2500, 3360, 3500],
));
body.push(P([
  it("In the schema but not used by any code: "),
  new TextRun("attachments, attendance, medical_records, medications and iep_goal_templates — these tables exist in schema.sql but have no controller, service, DAO, or mapper."),
]));

body.push(H2("2.6 Access Control Design"));
body.push(P([
  new TextRun("Access is decided by a single class, "), bold("AccessControl"),
  new TextRun(", which answers one question: “may this user act on this student?” Because every student-scoped record carries a studentId, this one check protects reports, incidents, IEPs, therapy and messaging alike. The rule is intentionally small and centralised so it can be reasoned about and unit-tested in isolation."),
]));
body.push(img("uml_activity.png", 360, "Access control activity"));
body.push(caption("Figure 4. Access-control decision flow."));
body.push(table(
  ["Role", "Visibility rule"],
  [
    ["ADMIN", "Every student (unconditional)."],
    ["TEACHER", "Only students in their caseload (teacher_student)."],
    ["PARENT", "Only their own children (parent_student)."],
    ["(other / null)", "Denied."],
  ],
  [2200, 7160],
));
body.push(P([
  new TextRun("The enforcement is layered. The "), bold("service layer"),
  new TextRun(" calls "), mono("access.require(user, studentId)"),
  new TextRun(" before returning or mutating student data; the "), bold("controller layer"),
  new TextRun(" also guards by role (e.g. only staff may author reports); and the "), bold("URL layer"),
  new TextRun(" restricts admin-only areas in the Spring Security filter chain. This defence-in-depth means a single forgotten check in one layer cannot by itself expose data."),
]));

body.push(H2("2.7 Security Design"));
body.push(bullet([bold("Authentication: "), new TextRun("Spring Security form login against the users table; passwords are verified against existing jBCrypt $2a$ hashes by BCryptPasswordEncoder.")]));
body.push(bullet([bold("Authorisation: "), new TextRun("each role becomes a ROLE_<role> granted authority, used by URL matchers, controller guards and Thymeleaf sec:authorize attributes.")]));
body.push(bullet([bold("CSRF: "), new TextRun("left enabled (Spring default); every Thymeleaf POST form carries the CSRF token automatically.")]));
body.push(bullet([bold("Account lifecycle: "), new TextRun("deactivated accounts (active = false) are rejected at login via UserDetails.isEnabled().")]));
body.push(bullet([bold("Least privilege: "), new TextRun("administrators do not use direct messaging; parents are read-only on reports/incidents; deactivated users cannot authenticate.")]));

body.push(H2("2.8 Transaction & Data Integrity Design"));
body.push(P([
  new TextRun("By default the MyBatis helper opens one session per call and commits immediately. Multi-step writes therefore needed an explicit transaction boundary, otherwise a failure mid-sequence could leave a half-written record (a report without its rubrics, a goal without its seed snapshot). The solution is a thread-bound transaction helper, "),
  mono("MyBatis.tx(...)"),
  new TextRun(": it binds a single non-auto-commit SqlSession to the current thread, every nested DAO call joins it, and the whole block commits once, or rolls back entirely if any exception escapes. Calls nest safely."),
]));
body.push(table(
  ["Operation", "Steps made atomic"],
  [
    ["Create daily report", "insert report + insert each rubric row"],
    ["Update daily report", "update report + delete old rubrics + re-insert rubrics"],
    ["Add / update IEP goal", "insert/update goal + insert progress snapshot"],
    ["Notify parents", "fan-out one notification per guardian (all or none)"],
  ],
  [3000, 6360],
));
body.push(P([
  new TextRun("Validation is performed "), it("before"),
  new TextRun(" the transaction opens (e.g. rubric ratings are checked first) so that invalid input never causes even a transient write."),
]));

body.push(H2("2.9 Notification & Event-Driven Design"));
body.push(P("Side-effects that should not block the user (notifying parents when a report is published, or when a parent-visible incident is logged) are modelled as Spring application events. The controller publishes an event; an asynchronous listener calls NotificationService, which fans out one notification per guardian inside a transaction. A scheduled job also sends therapy reminders every morning at 07:00. Notification types are limited to a fixed ENUM (DAILY_REPORT, INCIDENT, THERAPY_REMINDER, MESSAGE, GENERAL) that mirrors the database column."));

body.push(H2("2.10 Presentation & UI Design"));
body.push(P([
  new TextRun("All pages share a single Thymeleaf layout ("), mono("layout/main.html"),
  new TextRun(") built with the Layout Dialect. A fixed icon “rail” provides role-aware navigation: links appear only for roles permitted to use them, via "),
  mono("sec:authorize"),
  new TextRun(". The visual language (colour tokens, spacing and typography) is defined once in "),
  mono("app.css"),
  new TextRun(" and applied everywhere, with a warmer, child-centred dashboard for parents. The dashboard presents progress as cards and proportional bars rather than charts, and the notification bell polls an unread-count endpoint every 30 seconds."),
]));

body.push(H2("2.11 Key Workflow — Daily Report Lifecycle"));
body.push(P("The daily report is the system’s most representative workflow: it touches access control, validation, transactions, the draft/publish state machine, events and notifications. A teacher creates a draft (report + rubric rows written atomically), edits it while it remains a draft, then publishes it — at which point parents can see it and are notified. Parents may acknowledge a published report, which notifies the authoring teacher."));
body.push(img("uml_sequence.png", 620, "Sequence diagram"));
body.push(caption("Figure 5. Sequence of creating and publishing a daily report."));
body.push(P("The report’s permitted state transitions are summarised as a state machine. A report is born DRAFT, may be edited freely while it remains a draft, and only becomes visible to parents when PUBLISHED (which fires the notification event). Publishing is reversible via unpublish; a published report can also be acknowledged by a parent, which notifies the authoring teacher."));
body.push(img("uml_state.png", 540, "Report state machine"));
body.push(caption("Figure 6. Daily report lifecycle state machine."));

body.push(H2("2.12 Validation & Error Handling"));
body.push(P([
  new TextRun("Business rules raise "), mono("ValidationException"),
  new TextRun(" or "), mono("AccessDeniedException"),
  new TextRun(", which a single "), mono("@ControllerAdvice"),
  new TextRun(" ("), mono("GlobalExceptionHandler"),
  new TextRun(") renders as friendly pages with the correct HTTP status (400 for validation, 403 for access denied). JSON endpoints such as the calendar feed return a clean 400 on malformed input rather than a 500."),
]));

body.push(H2("2.13 Design Patterns Applied"));
body.push(P("The codebase leans on a small set of well-understood patterns rather than novel abstractions, which keeps it approachable and testable."));
body.push(table(
  ["Pattern", "Where", "Purpose"],
  [
    ["Layered (n-tier)", "web → service → dao → util", "Separation of concerns; dependencies point downward only."],
    ["Data Access Object", "*Dao / *DaoImpl", "Isolate persistence behind interfaces; SQL kept in mapper XML."],
    ["Service Layer", "*Service classes", "Encapsulate business rules, validation and transactions."],
    ["Front Controller", "Spring DispatcherServlet", "Single entry point routing to @Controller handlers."],
    ["Service Locator + DI bridge", "Services + LegacyServicesConfig", "Reuse hand-wired services as Spring beans."],
    ["Observer / Pub-Sub", "ApplicationEventPublisher + listener", "Decouple side-effects (notifications) from the request."],
    ["Template Method", "MyBatis.tx(...) wrapping a body", "Fix the commit/rollback skeleton; vary the work."],
    ["Singleton", "MyBatis SqlSessionFactory", "One lazily-built, thread-safe factory per JVM."],
    ["Strategy (role)", "AccessControl switch on role", "Per-role visibility rule behind one interface."],
  ],
  [2300, 3200, 3860],
));

body.push(H2("2.14 Data Dictionary (selected tables)"));
body.push(P("The following catalogues give the column-level definition of the most important tables. PK = primary key, FK = foreign key, U = part of a unique constraint."));

function dict(title, rows) {
  body.push(H3(title));
  body.push(table(["Column", "Type", "Notes"], rows, [2600, 2900, 3860]));
}
dict("2.14.1 users", [
  ["id", "BIGINT", "PK, auto-increment"],
  ["username", "VARCHAR(50)", "U — login name"],
  ["password_hash", "VARCHAR(100)", "BCrypt $2a$ hash"],
  ["full_name", "VARCHAR(120)", "Display name"],
  ["email", "VARCHAR(120)", "U"],
  ["phone", "VARCHAR(30)", "Nullable"],
  ["role", "ENUM", "ADMIN / TEACHER / PARENT"],
  ["active", "BOOLEAN", "Login allowed when true"],
  ["created_at", "DATETIME", "Defaults to now"],
]);
dict("2.14.2 students", [
  ["id", "BIGINT", "PK"],
  ["student_code", "VARCHAR(30)", "U — external code"],
  ["full_name", "VARCHAR(120)", ""],
  ["date_of_birth", "DATE", "Drives age display"],
  ["gender", "ENUM", "MALE / FEMALE / OTHER"],
  ["disability_type", "ENUM", "9 categories (AUTISM … OTHER)"],
  ["support_level", "ENUM", "LOW / MODERATE / HIGH / INTENSIVE"],
  ["enrollment_date", "DATE", ""],
  ["class_group", "VARCHAR(50)", "Nullable"],
  ["active", "BOOLEAN", "Soft-delete flag"],
]);
dict("2.14.3 daily_reports", [
  ["id", "BIGINT", "PK"],
  ["student_id", "BIGINT", "FK → students; U with report_date"],
  ["teacher_id", "BIGINT", "FK → users (author)"],
  ["report_date", "DATE", "U with student_id (one per day)"],
  ["summary", "TEXT", "Required written note"],
  ["mood", "ENUM", "HAPPY / CALM / ANXIOUS / FRUSTRATED / TIRED"],
  ["status", "ENUM", "DRAFT / PUBLISHED"],
  ["published_at", "DATETIME", "Set on publish"],
]);
dict("2.14.4 iep_goals", [
  ["id", "BIGINT", "PK"],
  ["iep_id", "BIGINT", "FK → iep_programs (cascade)"],
  ["domain", "ENUM", "ACADEMIC … VOCATIONAL (7)"],
  ["description", "VARCHAR(500)", "Measurable goal"],
  ["target_date", "DATE", "Nullable; shown on calendar"],
  ["status", "ENUM", "NOT_STARTED … DISCONTINUED"],
  ["progress_pct", "INT", "CHECK 0..100"],
]);
dict("2.14.5 therapy_sessions", [
  ["id", "BIGINT", "PK"],
  ["student_id", "BIGINT", "FK → students"],
  ["therapist_id", "BIGINT", "FK → users (the teacher running the session)"],
  ["therapy_type", "ENUM", "SPEECH / OCCUPATIONAL / PHYSICAL / BEHAVIORAL / COUNSELING"],
  ["start_time / end_time", "DATETIME", "CHECK end > start"],
  ["location", "VARCHAR(120)", "Room — conflict-checked"],
  ["status", "ENUM", "SCHEDULED / COMPLETED / CANCELLED / NO_SHOW"],
]);
dict("2.14.6 behavior_incidents", [
  ["id", "BIGINT", "PK"],
  ["student_id", "BIGINT", "FK → students"],
  ["reported_by", "BIGINT", "FK → users"],
  ["incident_date", "DATETIME", "When it happened"],
  ["description", "TEXT", "Required"],
  ["severity", "ENUM", "LOW / MEDIUM / HIGH / CRITICAL"],
  ["action_taken", "VARCHAR(500)", "Nullable"],
  ["parent_visible", "BOOLEAN", "Auto-true for HIGH/CRITICAL"],
]);

body.push(H3("2.14.7 Relationships & cardinality"));
body.push(table(
  ["Relationship", "Cardinality", "On delete"],
  [
    ["users — teacher_student — students", "M : N", "Cascade"],
    ["users — parent_student — students", "M : N", "Cascade"],
    ["students — iep_programs", "1 : N", "Cascade"],
    ["iep_programs — iep_goals", "1 : N", "Cascade"],
    ["iep_goals — goal_progress_history", "1 : N", "Cascade"],
    ["students — daily_reports", "1 : N", "Cascade"],
    ["daily_reports — report_rubrics", "1 : N", "Cascade"],
    ["daily_reports — report_acknowledgements", "1 : N", "Cascade"],
    ["students — behavior_incidents", "1 : N", "Cascade"],
    ["students — therapy_sessions", "1 : N", "Cascade"],
    ["users — messages (sender/recipient)", "1 : N", "Cascade"],
  ],
  [4400, 2500, 2460],
));

body.push(H2("2.15 Non-Functional Requirements"));
body.push(table(
  ["Quality", "How it is addressed"],
  [
    ["Security", "BCrypt, CSRF, ROLE_-based + student-scoped authorisation, deactivated-account rejection."],
    ["Data integrity", "Foreign keys + CHECK/UNIQUE constraints; atomic multi-step writes via MyBatis.tx."],
    ["Maintainability", "Layered packages, thin controllers, SQL in mapper XML, single access-control authority."],
    ["Usability / accessibility", "Role-aware navigation; warm, child-centred parent dashboard; consistent design tokens."],
    ["Performance", "HikariCP pooling; targeted indexes for role-scoped queries; lightweight 30s bell poll."],
    ["Reliability", "Centralised exception handling; validation before persistence; idempotent acknowledgement."],
    ["Testability", "Pure service objects with injected DAOs; JUnit 5 + Mockito unit tests."],
  ],
  [2400, 6960],
));
body.push(PB());

/* ================= 3. IMPLEMENTATION ================= */
body.push(H1("3. Implementation — Keycode / Screen"));
body.push(P("This section walks through the code that gives the design its teeth, then catalogues the screens that present it. Listings are taken verbatim from the source (lightly trimmed for space)."));

body.push(H2("3.1 Build & Run"));
body.push(...code([
  "# build + test",
  "mvn test",
  "# run (requires MySQL 8 with the sne_spims schema seeded)",
  "mvn spring-boot:run    # -> http://localhost:8080",
], "Listing 2. Build and run commands."));
body.push(P([
  new TextRun("The application boots from "), mono("SneSpimsApplication"),
  new TextRun(". Spring’s DataSource auto-configuration is excluded because the MyBatis layer owns its own HikariCP pool configured from "),
  mono("db.properties"), new TextRun("."),
]));

body.push(H2("3.2 Keycode Walkthrough"));

body.push(H3("3.2.1 Transaction helper (MyBatis.tx)"));
body.push(P("The heart of the data-integrity design. A ThreadLocal carries the active session so that ordinary DAO calls automatically join an open transaction without any change to their code."));
body.push(...code([
  "private static final ThreadLocal<SqlSession> CURRENT_TX = new ThreadLocal<>();",
  "",
  "public static <T> T tx(Supplier<T> body) {",
  "    if (CURRENT_TX.get() != null) {",
  "        return body.get();              // already in a tx -> join it",
  "    }",
  "    SqlSession session = factory().openSession(false); // explicit commit",
  "    CURRENT_TX.set(session);",
  "    try {",
  "        T result = body.get();",
  "        session.commit();",
  "        return result;",
  "    } catch (RuntimeException e) {",
  "        session.rollback();",
  "        throw e;",
  "    } finally {",
  "        CURRENT_TX.remove();",
  "        session.close();",
  "    }",
  "}",
  "",
  "private static <T> T write(String statement, SessionFn<T> fn) {",
  "    SqlSession tx = CURRENT_TX.get();",
  "    if (tx != null) return fn.apply(tx);          // commit deferred to tx()",
  "    try (SqlSession s = factory().openSession()) {",
  "        T result = fn.apply(s);",
  "        s.commit();                               // legacy per-call commit",
  "        return result;",
  "    }",
  "}",
], "Listing 3. com.school.util.MyBatis — transaction support."));

body.push(H3("3.2.2 Central access control"));
body.push(...code([
  "public boolean canAccess(User user, long studentId) {",
  "    if (user == null) return false;",
  "    switch (user.getRole()) {",
  "        case \"ADMIN\":   return true;",
  "        case \"TEACHER\": return assignments.isTeacherOfStudent(user.getId(), studentId);",
  "        case \"PARENT\":  return assignments.isParentOfStudent(user.getId(), studentId);",
  "        default:        return false;",
  "    }",
  "}",
  "",
  "/** Enforcing variant — throws when not allowed. */",
  "public void require(User user, long studentId) {",
  "    if (!canAccess(user, studentId))",
  "        throw new AccessDeniedException(\"You do not have access to this student’s records.\");",
  "}",
], "Listing 4. com.school.service.AccessControl."));

body.push(H3("3.2.3 Transactional service write with up-front validation"));
body.push(P("ReportService.createDraft shows the full pattern: authorise, validate, then perform the multi-row write inside a transaction so it is all-or-nothing."));
body.push(...code([
  "public long createDraft(User author, DailyReport report,",
  "                        Map<String,String> ratings, Map<String,String> comments) {",
  "    requireAuthor(author, report.getStudentId());      // role + student access",
  "    if (report.getReportDate() == null) throw new ValidationException(\"Report date is required.\");",
  "    if (isBlank(report.getSummary()))   throw new ValidationException(\"A summary is required.\");",
  "    if (reportDao.dateExists(report.getStudentId(), report.getReportDate(), 0))",
  "        throw new ValidationException(\"A report already exists for this student on that date.\");",
  "    validateRubrics(ratings);          // fail BEFORE any write",
  "    report.setStatus(\"DRAFT\");",
  "    return MyBatis.tx(() -> {          // report + rubric rows: atomic",
  "        long reportId = reportDao.insertReport(report);",
  "        saveRubrics(reportId, ratings, comments);",
  "        return reportId;",
  "    });",
  "}",
  "",
  "private void requireAuthor(User user, long studentId) {",
  "    if (user == null || user.isParent())",
  "        throw new AccessDeniedException(\"Only teachers and admins can author reports.\");",
  "    access.require(user, studentId);",
  "}",
], "Listing 5. com.school.service.ReportService — createDraft."));

body.push(H3("3.2.4 Thin controller with role guard"));
body.push(...code([
  "@PostMapping(\"/students/{studentId}/reports\")",
  "public String create(@PathVariable long studentId, @RequestParam ... , RedirectAttributes ra) {",
  "    User user = SecurityUtils.currentUser();",
  "    requireStaff(user);                       // role guard at the controller",
  "    students.get(user, studentId);            // student-access check",
  "    DailyReport r = new DailyReport();",
  "    r.setStudentId(studentId);  r.setTeacherId(user.getId());",
  "    r.setReportDate(reportDate); r.setSummary(summary); r.setMood(mood);",
  "    long id = reports.createDraft(user, r, toMap(criterion, rating), toMap(criterion, comment));",
  "    ra.addFlashAttribute(\"flashSuccess\", \"Report saved as draft.\");",
  "    return \"redirect:/reports/\" + id;",
  "}",
], "Listing 6. com.school.web.ReportController — create handler."));

body.push(H3("3.2.5 DAO + MyBatis mapper"));
body.push(P("DAO implementations are thin: they call the shared helper with a statement id and parameters; the SQL lives in XML mapper files, keeping Java free of string SQL."));
body.push(...code([
  "// AssignmentDaoImpl.java",
  "public boolean isTeacherOfStudent(long teacherId, long studentId) {",
  "    return MyBatis.count(\"AssignmentMapper.isTeacherOfStudent\",",
  "            MyBatis.args(\"teacherId\", teacherId, \"studentId\", studentId)) > 0;",
  "}",
], "Listing 7a. DAO implementation."));
body.push(...code([
  "<!-- AssignmentMapper.xml -->",
  "<select id=\"isTeacherOfStudent\" resultType=\"long\">",
  "    SELECT COUNT(*) FROM teacher_student",
  "    WHERE teacher_id = #{teacherId} AND student_id = #{studentId}",
  "</select>",
], "Listing 7b. Corresponding mapper statement (parameterised — no SQL injection)."));

body.push(H3("3.2.6 Security configuration"));
body.push(...code([
  "@Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }",
  "",
  "@Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {",
  "    http.authorizeHttpRequests(auth -> auth",
  "            .requestMatchers(\"/css/**\",\"/js/**\",\"/images/**\",\"/webjars/**\",\"/favicon.ico\",\"/error\").permitAll()",
  "            .requestMatchers(\"/login\").permitAll()",
  "            .requestMatchers(\"/users/**\",\"/announcements/**\").hasRole(\"ADMIN\")  // admin-only",
  "            .anyRequest().authenticated())",
  "        .formLogin(f -> f.loginPage(\"/login\").defaultSuccessUrl(\"/dashboard\", true).permitAll())",
  "        .logout(l -> l.logoutUrl(\"/logout\").logoutSuccessUrl(\"/login?logout\").permitAll());",
  "    return http.build();",
  "}",
], "Listing 8. com.school.security.SecurityConfig."));

body.push(H3("3.2.7 Event-driven notifications"));
body.push(...code([
  "// Controller publishes after a successful publish:",
  "events.publishEvent(new DailyReportPublishedEvent(r.getStudentId(), r.getStudentName(), r.getId()));",
  "",
  "// NotificationService fans out atomically to every guardian:",
  "public void notifyParentsOfStudent(long studentId, String type, String title,",
  "                                   String message, String link) {",
  "    MyBatis.tx(() -> {",
  "        for (Assignment link2 : assignments.parentLinks(studentId))",
  "            notifyUser(link2.getUserId(), type, title, message, link, studentId);",
  "    });",
  "}",
], "Listing 9. Event publication and notification fan-out."));

body.push(H3("3.2.8 Role-aware Thymeleaf layout"));
body.push(...code([
  "<!-- layout/main.html : navigation links appear only for permitted roles -->",
  "<a class=\"rail-btn\" sec:authorize=\"hasAnyRole('ADMIN','TEACHER')\"",
  "   th:href=\"@{/students}\" title=\"Students\">",
  "    <i class=\"bi bi-people\"></i>",
  "</a>",
  "<a class=\"rail-btn\" sec:authorize=\"hasAnyRole('TEACHER','PARENT')\"",
  "   th:href=\"@{/messages}\" title=\"Messages\">",
  "    <i class=\"bi bi-chat-left-text\"></i>",
  "</a>",
], "Listing 10. Role-aware navigation in the shared layout."));

body.push(H3("3.2.9 Therapy scheduling — three-way conflict detection"));
body.push(P("Scheduling validates the session, then rejects any clash on the student, the teacher running it, or the room. A cancelled session never blocks, and the message names exactly what conflicts."));
body.push(...code([
  "private void checkConflicts(TherapySession s) {",
  "    long exclude = s.getId();          // 0 for a new session -> excludes nothing",
  "    if (therapyDao.hasConflict(s.getStudentId(), s.getStartTime(), s.getEndTime(), exclude))",
  "        throw new ValidationException(\"This student already has a session in that time slot.\");",
  "    if (s.getTherapistId() > 0 &&",
  "        therapyDao.hasTherapistConflict(s.getTherapistId(), s.getStartTime(), s.getEndTime(), exclude))",
  "        throw new ValidationException(\"That teacher/therapist is already booked in that time slot.\");",
  "    if (s.getLocation() != null && !s.getLocation().isBlank() &&",
  "        therapyDao.hasRoomConflict(s.getLocation(), s.getStartTime(), s.getEndTime(), exclude))",
  "        throw new ValidationException(\"Room is already booked in that time slot.\");",
  "}",
], "Listing 11. com.school.service.TherapyService — conflict detection."));

body.push(H3("3.2.10 Current-user injection & request context"));
body.push(P("SecurityUtils reads the authenticated user from the security context; a controller advice then injects it (plus the school name and current path) into every view’s model, so templates never reach into security directly."));
body.push(...code([
  "// SecurityUtils — usable from any layer",
  "public static User currentUser() {",
  "    Authentication auth = SecurityContextHolder.getContext().getAuthentication();",
  "    if (auth != null && auth.getPrincipal() instanceof AppUserDetails d) return d.getUser();",
  "    return null;",
  "}",
  "",
  "// CurrentUserAdvice — shared model attributes for all controllers",
  "@ControllerAdvice",
  "public class CurrentUserAdvice {",
  "    @ModelAttribute(\"currentUser\") public User currentUser() { return SecurityUtils.currentUser(); }",
  "    @ModelAttribute(\"currentPath\") public String path(HttpServletRequest r) { return r.getRequestURI(); }",
  "}",
], "Listing 12. Authenticated-user access and model injection."));

body.push(H3("3.2.11 Scheduled therapy reminder (cron)"));
body.push(...code([
  "@Component",
  "public class TherapyReminderJob {",
  "    @Scheduled(cron = \"0 0 7 * * *\")          // every day at 07:00",
  "    public void run() {",
  "        int sent = notifications.sendTherapyReminders();",
  "        log.info(\"Therapy reminder job sent {} therapist notifications\", sent);",
  "    }",
  "}",
], "Listing 13. com.school.notify.TherapyReminderJob."));

body.push(H3("3.2.12 Centralised exception handling"));
body.push(...code([
  "@ControllerAdvice",
  "public class GlobalExceptionHandler {",
  "    @ExceptionHandler(AccessDeniedException.class) @ResponseStatus(HttpStatus.FORBIDDEN)",
  "    public String denied(AccessDeniedException e, Model m) {",
  "        m.addAttribute(\"message\", e.getMessage()); return \"common/denied\";",
  "    }",
  "    @ExceptionHandler(ValidationException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)",
  "    public String validation(ValidationException e, Model m) {",
  "        m.addAttribute(\"message\", e.getMessage()); return \"common/message\";",
  "    }",
  "}",
], "Listing 14. com.school.web.GlobalExceptionHandler."));

body.push(H2("3.3 Testing"));
body.push(P("Automated tests focus on the security-critical and integrity-critical logic: the central access rules and the report service’s authorisation and validation ordering. The services are plain objects with injected DAOs, so they unit-test cleanly with Mockito — no database or Spring context required. The whole suite (15 tests) passes via mvn test."));
body.push(...code([
  "@Test @DisplayName(\"rejects an invalid rating before writing anything\")",
  "void invalidRatingWritesNothing() {",
  "    when(assignments.isTeacherOfStudent(TEACHER_ID, STUDENT_ID)).thenReturn(true);",
  "    DailyReport r = report(\"DRAFT\");",
  "    r.setReportDate(LocalDate.now()); r.setSummary(\"Good day.\");",
  "    assertThatThrownBy(() -> service.createDraft(user(TEACHER_ID, \"TEACHER\"), r,",
  "            Map.of(\"Academic Engagement\", \"NOT_A_RATING\"), Map.of()))",
  "        .isInstanceOf(ValidationException.class);",
  "    verify(reportDao, never()).insertReport(r);   // no orphan draft",
  "}",
], "Listing 15. ReportServiceTest — validation happens before any write."));
body.push(table(
  ["Test class", "What it verifies"],
  [
    ["AccessControlTest", "Null user denied; ADMIN allowed; TEACHER/PARENT allowed only for their own students; require() throws when denied."],
    ["ReportServiceTest", "Parents cannot see drafts; staff can; acknowledgement rules; invalid ratings and parent authoring rejected before any write."],
  ],
  [2600, 6760],
));

body.push(H2("3.4 Screen Catalogue"));
body.push(P("The interface is organised around the icon rail. The table below maps each principal screen to its route, the roles that may reach it, and its purpose."));
body.push(table(
  ["Screen", "Route", "Roles", "Purpose"],
  [
    ["Login", "/login", "All", "Form authentication."],
    ["Dashboard (staff)", "/dashboard", "Admin, Teacher", "Analytics, sessions, incidents, reports-due, calendar strip."],
    ["Dashboard (parent)", "/dashboard", "Parent", "Child-centred view: latest report, progress, sessions, week."],
    ["Students", "/students", "Admin, Teacher", "Roster (role-scoped), search & filters."],
    ["Student detail", "/students/{id}", "Admin, Teacher", "Profile, assignments, links to records."],
    ["Daily reports", "/students/{id}/reports", "All (scoped)", "List; staff author, parents read published."],
    ["Report form", "/reports/{id}/edit", "Admin, Teacher", "Author/edit report + rubric scoring."],
    ["IEP list / detail", "/iep, /iep/{id}", "All (scoped)", "Programs and goals grouped by domain + progress."],
    ["Incidents", "/incidents", "Admin, Teacher, Parent*", "Log/view incidents (parents: high-severity/shared)."],
    ["Therapy", "/therapy", "Admin, Teacher", "Schedule and manage therapy sessions."],
    ["Messages", "/messages", "Teacher, Parent", "Per-room parent–teacher chat anchored to a student."],
    ["Calendar", "/calendar", "All (scoped)", "Therapy + goal dates + school events feed."],
    ["Notifications", "/notifications", "All", "Inbox + bell badge (30s poll)."],
    ["Users", "/users", "Admin", "Account CRUD, password reset, activation."],
    ["Announcements", "/announcements", "Admin", "Role-targeted notices."],
    ["Account", "/account", "All", "Self-service profile and password change."],
  ],
  [2050, 2200, 1850, 3260],
));

body.push(H2("3.5 Selected Screens"));

body.push(H3("3.5.1 Role-aware Dashboard"));
body.push(P("The dashboard branches on role. Staff see operational widgets built entirely from live data; parents instead see a warm, child-centred layout. A child switcher supports parents with several children."));
body.push(bullet("Staff: progress-by-area cards, disability distribution, this week’s therapy sessions, recent incidents (28-day window)."));
body.push(bullet("Teacher: unread-message count and a “reports due” list (caseload children with no published report in 7 days)."));
body.push(bullet("Admin: teacher/parent head-counts, sessions this week, upcoming school events (14-day window)."));
body.push(bullet("Parent: latest published report, progress per developmental domain, next two therapy sessions, interactive week strip."));

body.push(H3("3.5.2 Daily Report Form"));
body.push(P("The report form captures a written summary, the child’s mood, and a rating per rubric criterion. Ratings are validated against a fixed vocabulary before anything is written; the report and its rubric rows are saved together in one transaction."));
body.push(bullet("Fields: report date, summary (required), mood, and a rating + comment for each of five criteria."));
body.push(bullet("Criteria: Academic Engagement, Social Interaction, Emotional Regulation, Motor Skills, Communication."));
body.push(bullet("Ratings: EMERGING, DEVELOPING, PROFICIENT, MASTERED. Lifecycle: DRAFT → PUBLISHED (parents notified) → optionally ACKNOWLEDGED."));

body.push(H3("3.5.3 IEP Detail"));
body.push(P("An IEP program lists its measurable goals grouped by developmental domain, each with a status and a progress percentage. Updating a goal records a progress snapshot so the trend can be charted over time; goal and snapshot are written atomically."));
body.push(bullet("Domains: Academic, Communication, Social-Emotional, Motor, Behavioural, Self-care, Vocational."));
body.push(bullet("Each goal shows status (Not started → Achieved/Discontinued) and a 0–100% progress bar."));

body.push(H3("3.5.4 Behaviour Incidents"));
body.push(P("Staff log incidents with a severity; the list is role-scoped. Parents see only incidents that are explicitly shared or are HIGH/CRITICAL (which are auto-shared), and the filtering happens in the SQL query, never by fetching then hiding."));

body.push(H3("3.5.5 Messaging"));
body.push(P("Messaging is presented as chat “rooms”. A teacher sees one room per (parent, child); a parent sees one merged room per teacher across their children. Valid recipients are derived from assignment relationships, so a user can only ever message a legitimate contact about a student they share."));

body.push(H3("3.5.6 Calendar"));
body.push(P("A unified calendar merges therapy sessions, IEP goal target dates and school-wide events into one FullCalendar feed. The feed is role-scoped: the set of students whose events appear is exactly the caller’s visible roster, and an out-of-scope student id in the query is ignored rather than honoured."));

body.push(H3("3.5.7 Notifications"));
body.push(P("The inbox lists a user’s notifications (with archive/mark-read actions); a navbar bell badge polls an unread-count endpoint every 30 seconds. Notifications are created by published reports, parent-visible incidents, new messages and the 07:00 therapy-reminder job."));

body.push(H3("3.5.8 Administration (Users & Announcements)"));
body.push(P("Admin-only screens manage accounts (create, edit, reset password, activate/deactivate) and role-targeted announcements (ALL / TEACHER / PARENT) with optional publish and expiry times. Both are protected at the URL layer and again in the controller."));
body.push(PB());

/* ================= 4. SUMMARY ================= */
body.push(H1("4. Summary"));
body.push(P("SNE-SPIMS moves a special-needs school information system onto Spring Boot 3 while keeping its MyBatis data layer and existing login credentials. What you get is a layered web application that covers student records, IEPs, daily reports, behaviour incidents, therapy scheduling, parent–teacher messaging, a unified calendar, role-aware dashboards and a notification inbox."));
body.push(P([bold("What the build does well:")]));
body.push(bullet([bold("Centralised access control: "), new TextRun("a single AccessControl class decides student visibility for every module, enforced in depth across service, controller and URL layers.")]));
body.push(bullet([bold("Data integrity: "), new TextRun("a thread-bound MyBatis.tx makes every multi-step write atomic, eliminating the half-written records that the original per-call commit allowed.")]));
body.push(bullet([bold("Clean separation: "), new TextRun("thin controllers, business rules in services, SQL in mapper XML, and a shared Thymeleaf layout with role-aware navigation.")]));
body.push(bullet([bold("Security by default: "), new TextRun("BCrypt verification, CSRF protection, ROLE_-based authorisation and rejection of deactivated accounts.")]));
body.push(bullet([bold("Tested behaviour: "), new TextRun("the access rules and the report service’s authorisation and validation ordering are covered by JUnit 5 / Mockito tests.")]));
body.push(P([bold("Limitations & future work. "), new TextRun("Several legacy modules (file attachments, attendance recap, medical management, the IEP goal-bank) already have their database tables but are not yet ported to Spring MVC. Test coverage is solid on the security-critical paths, but the DAO and transaction layers would be better exercised end-to-end with an in-memory database. These are the obvious next steps; what ships today already works.")]));
body.push(P([new TextRun("The application is in a working, deployable state: access is role-scoped, multi-step writes are transactional, the layers are cleanly separated, it builds with Maven, and all 15 tests pass.")]));

/* ============================================================ DOC */
const doc = new Document({
  creator: "SNE-SPIMS",
  title: "SNE-SPIMS Technical Document",
  styles: {
    default: { document: { run: { font: "Arial", size: 21, color: INK } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, color: BLUE, font: "Arial" },
        paragraph: { spacing: { before: 300, after: 200 }, outlineLevel: 0,
          border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: "BCCCDC", space: 4 } } } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 26, bold: true, color: "243B53", font: "Arial" },
        paragraph: { spacing: { before: 220, after: 120 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 22, bold: true, color: "334E68", font: "Arial" },
        paragraph: { spacing: { before: 160, after: 80 }, outlineLevel: 2 } },
    ],
  },
  numbering: {
    config: [
      { reference: "bullets", levels: [
        { level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 540, hanging: 280 } } } },
        { level: 1, format: LevelFormat.BULLET, text: "–", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 980, hanging: 280 } } } },
      ] },
      { reference: "nums", levels: [
        { level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 540, hanging: 280 } } } },
      ] },
    ],
  },
  sections: [{
    properties: { page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } } },
    headers: { default: new Header({ children: [new Paragraph({
      alignment: AlignmentType.RIGHT,
      border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: "DCE3EA", space: 2 } },
      children: [new TextRun({ text: "SNE-SPIMS — Technical Document", size: 16, color: "9FB3C8" })] })] }) },
    footers: { default: new Footer({ children: [new Paragraph({
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text: "Page ", size: 16, color: "9FB3C8" }),
        new TextRun({ children: [PageNumber.CURRENT], size: 16, color: "9FB3C8" }),
        new TextRun({ text: " of ", size: 16, color: "9FB3C8" }),
        new TextRun({ children: [PageNumber.TOTAL_PAGES], size: 16, color: "9FB3C8" })] })] }) },
    children: body,
  }],
});

Packer.toBuffer(doc).then((buf) => {
  const out = path.join(__dirname, "SNE-SPIMS_Technical_Document.docx");
  fs.writeFileSync(out, buf);
  console.log("Wrote", out, "(" + buf.length + " bytes)");
});

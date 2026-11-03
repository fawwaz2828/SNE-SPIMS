-- ============================================================================
-- SNE-SPIMS : SNE Student Progress and Information Management System
-- MySQL 8 schema  (14 interrelated tables)
-- Run order:  schema.sql  then  seed.sql
--
-- !!! DANGER — DESTRUCTIVE !!!
-- The next statement DROPS the entire `sne_spims` database. Running this file
-- WIPES ALL LIVE DATA. It is meant only for a clean rebuild/reseed.
-- Do NOT pipe this file through a `USE`-swap (e.g. sed) to target another DB:
-- the DROP/CREATE DATABASE lines below are hard-coded to `sne_spims` and will
-- still hit the live database. To seed a scratch DB, edit these two lines too.
-- ============================================================================

DROP DATABASE IF EXISTS sne_spims;
CREATE DATABASE sne_spims CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sne_spims;

-- ---------------------------------------------------------------------------
-- 1. users  : all system accounts (admin / teacher / parent)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,                 -- BCrypt hash
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(120) NOT NULL,
    phone         VARCHAR(30),
    role          ENUM('ADMIN','TEACHER','PARENT') NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email)
);

-- ---------------------------------------------------------------------------
-- 2. students : SNE student records
-- ---------------------------------------------------------------------------
CREATE TABLE students (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_code    VARCHAR(30)  NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    date_of_birth   DATE         NOT NULL,
    gender          ENUM('MALE','FEMALE','OTHER') NOT NULL,
    disability_type ENUM('AUTISM','INTELLECTUAL','HEARING','VISUAL','PHYSICAL',
                         'SPEECH_LANGUAGE','LEARNING','MULTIPLE','OTHER') NOT NULL,
    support_level   ENUM('LOW','MODERATE','HIGH','INTENSIVE') NOT NULL,
    enrollment_date DATE         NOT NULL,
    class_group     VARCHAR(50),
    notes           TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_students_code UNIQUE (student_code)
);

-- ---------------------------------------------------------------------------
-- 3. teacher_student : assignment of teachers to students (many-to-many)
-- ---------------------------------------------------------------------------
CREATE TABLE teacher_student (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id  BIGINT NOT NULL,
    student_id  BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_primary  BOOLEAN  NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ts_teacher FOREIGN KEY (teacher_id) REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_ts_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uq_teacher_student UNIQUE (teacher_id, student_id)
);

-- ---------------------------------------------------------------------------
-- 4. parent_student : assignment of parents/guardians to their child
-- ---------------------------------------------------------------------------
CREATE TABLE parent_student (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id    BIGINT NOT NULL,
    student_id   BIGINT NOT NULL,
    relationship ENUM('MOTHER','FATHER','GUARDIAN','OTHER') NOT NULL,
    assigned_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ps_parent  FOREIGN KEY (parent_id)  REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_ps_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT uq_parent_student UNIQUE (parent_id, student_id)
);

-- ---------------------------------------------------------------------------
-- 5. iep_programs : Individualized Education Programs (one active per student)
-- ---------------------------------------------------------------------------
CREATE TABLE iep_programs (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    title         VARCHAR(150) NOT NULL,
    academic_year VARCHAR(20)  NOT NULL,
    start_date    DATE NOT NULL,
    end_date      DATE NOT NULL,
    present_levels TEXT NULL,                            -- baseline (PLAAFP / asesmen awal)
    accommodations TEXT NULL,                            -- accommodations & modifications
    status        ENUM('DRAFT','ACTIVE','COMPLETED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_by    BIGINT NOT NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_iep_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_iep_creator FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT uq_iep_student_year UNIQUE (student_id, academic_year)
);

-- ---------------------------------------------------------------------------
-- 6. iep_goals : measurable goals belonging to an IEP, grouped by domain
-- ---------------------------------------------------------------------------
CREATE TABLE iep_goals (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    iep_id       BIGINT NOT NULL,
    domain       ENUM('ACADEMIC','COMMUNICATION','SOCIAL_EMOTIONAL','MOTOR',
                      'BEHAVIORAL','SELF_CARE','VOCATIONAL') NOT NULL,
    description  VARCHAR(500) NOT NULL,
    target_date  DATE,
    status       ENUM('NOT_STARTED','IN_PROGRESS','ACHIEVED','DISCONTINUED')
                      NOT NULL DEFAULT 'NOT_STARTED',
    progress_pct INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_goal_iep FOREIGN KEY (iep_id) REFERENCES iep_programs(id) ON DELETE CASCADE,
    CONSTRAINT chk_goal_pct CHECK (progress_pct BETWEEN 0 AND 100)
);

-- ---------------------------------------------------------------------------
-- 7. daily_reports : teacher's daily progress report for a student
-- ---------------------------------------------------------------------------
CREATE TABLE daily_reports (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    teacher_id   BIGINT NOT NULL,
    report_date  DATE NOT NULL,
    summary      TEXT NOT NULL,
    mood         ENUM('HAPPY','CALM','ANXIOUS','FRUSTRATED','TIRED') ,
    status       ENUM('DRAFT','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
    published_at DATETIME,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_report_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_report_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT uq_report_student_date UNIQUE (student_id, report_date)
);

-- ---------------------------------------------------------------------------
-- 8. report_rubrics : per-criterion ratings attached to a daily report
-- ---------------------------------------------------------------------------
CREATE TABLE report_rubrics (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id   BIGINT NOT NULL,
    criterion   VARCHAR(120) NOT NULL,
    rating      ENUM('EMERGING','DEVELOPING','PROFICIENT','MASTERED') NOT NULL,
    comment     VARCHAR(300),
    CONSTRAINT fk_rubric_report FOREIGN KEY (report_id) REFERENCES daily_reports(id) ON DELETE CASCADE,
    CONSTRAINT uq_rubric_report_criterion UNIQUE (report_id, criterion)
);

-- ---------------------------------------------------------------------------
-- 9. behavior_incidents : logged behavioural incidents
-- ---------------------------------------------------------------------------
CREATE TABLE behavior_incidents (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    reported_by     BIGINT NOT NULL,
    incident_date   DATETIME NOT NULL,
    location        VARCHAR(120),
    description     TEXT NOT NULL,
    severity        ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
    action_taken    VARCHAR(500),
    parent_visible  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inc_student  FOREIGN KEY (student_id)  REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_inc_reporter FOREIGN KEY (reported_by) REFERENCES users(id)
);

-- ---------------------------------------------------------------------------
-- 12. therapy_sessions : scheduled therapy sessions (conflict-checked)
-- ---------------------------------------------------------------------------
CREATE TABLE therapy_sessions (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id    BIGINT NOT NULL,
    therapist_id  BIGINT NOT NULL,
    therapy_type  ENUM('SPEECH','OCCUPATIONAL','PHYSICAL','BEHAVIORAL','COUNSELING') NOT NULL,
    start_time    DATETIME NOT NULL,
    end_time      DATETIME NOT NULL,
    location      VARCHAR(120),
    status        ENUM('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'SCHEDULED',
    notes         VARCHAR(500),
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ths_student   FOREIGN KEY (student_id)   REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_ths_therapist FOREIGN KEY (therapist_id) REFERENCES users(id),
    CONSTRAINT chk_ths_time CHECK (end_time > start_time)
);

-- ---------------------------------------------------------------------------
-- 13. messages : direct messages between users (teacher <-> parent, admin, ...)
-- ---------------------------------------------------------------------------
CREATE TABLE messages (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id   BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    student_id  BIGINT,                 -- optional context student
    subject     VARCHAR(150) NOT NULL,
    body        TEXT NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_sender    FOREIGN KEY (sender_id)    REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_student   FOREIGN KEY (student_id)   REFERENCES students(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------------------------
-- 14. announcements : admin announcements, targeted by role
-- ---------------------------------------------------------------------------
CREATE TABLE announcements (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    body         TEXT NOT NULL,
    target_role  ENUM('ALL','TEACHER','PARENT') NOT NULL DEFAULT 'ALL',
    created_by   BIGINT NOT NULL,
    publish_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at   DATETIME,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ann_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Helpful indexes for role-scoped queries
CREATE INDEX idx_reports_student   ON daily_reports(student_id, status);
CREATE INDEX idx_incidents_student ON behavior_incidents(student_id, parent_visible, severity);
CREATE INDEX idx_therapy_therapist ON therapy_sessions(therapist_id, start_time);
CREATE INDEX idx_messages_recipient ON messages(recipient_id, is_read);
CREATE INDEX idx_announce_role     ON announcements(target_role, active);

-- ============================================================================
-- v2 additions — Spring Boot feature set (tables 15–22)
-- For an existing database, run only this section (see db/migration_v2.sql).
-- ============================================================================

-- 15. iep_goal_progress_history : periodic snapshots of goal progress (Feature 1)
CREATE TABLE iep_goal_progress_history (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id      BIGINT NOT NULL,
    recorded_on  DATE   NOT NULL,
    progress_pct INT    NOT NULL,
    note         VARCHAR(300),
    recorded_by  BIGINT NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gph_goal     FOREIGN KEY (goal_id)     REFERENCES iep_goals(id) ON DELETE CASCADE,
    CONSTRAINT fk_gph_recorder FOREIGN KEY (recorded_by) REFERENCES users(id),
    CONSTRAINT chk_gph_pct CHECK (progress_pct BETWEEN 0 AND 100),
    CONSTRAINT uq_gph_goal_date UNIQUE (goal_id, recorded_on)
);
CREATE INDEX idx_gph_goal_date ON iep_goal_progress_history(goal_id, recorded_on);

-- 16. notifications : per-user event notifications (Feature 2)
CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    type        ENUM('DAILY_REPORT','INCIDENT','THERAPY_REMINDER','MESSAGE','GENERAL') NOT NULL,
    title       VARCHAR(150) NOT NULL,
    message     VARCHAR(500) NOT NULL,
    link        VARCHAR(300),
    student_id  BIGINT,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL
);
CREATE INDEX idx_notif_user ON notifications(user_id, is_read, is_archived, created_at);

-- 17. attachments : polymorphic file attachments (Feature 4)
CREATE TABLE attachments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type    VARCHAR(40)  NOT NULL,   -- MILESTONE_ASSESSMENT | DAILY_REPORT | ...
    entity_id      BIGINT       NOT NULL,
    evidence_label VARCHAR(120),            -- e.g. "Bukti karya motorik"
    file_name      VARCHAR(255) NOT NULL,
    file_path      VARCHAR(500) NOT NULL,
    file_size      BIGINT       NOT NULL,
    mime_type      VARCHAR(120) NOT NULL,
    uploaded_by    BIGINT       NOT NULL,
    uploaded_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att_user FOREIGN KEY (uploaded_by) REFERENCES users(id)
);
CREATE INDEX idx_att_entity ON attachments(entity_type, entity_id);

-- 18. school_calendar : school-wide calendar events (Feature 5)
CREATE TABLE school_calendar (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_date  DATE         NOT NULL,
    event_name  VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    created_by  BIGINT,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cal_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_cal_date ON school_calendar(event_date);

-- 19. attendance : daily student attendance (Feature 6)
CREATE TABLE attendance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    attendance_date DATE   NOT NULL,
    status          ENUM('PRESENT','EXCUSED','SICK','ABSENT') NOT NULL,
    notes           VARCHAR(300),
    recorded_by     BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_att2_student  FOREIGN KEY (student_id)  REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_att2_recorder FOREIGN KEY (recorded_by) REFERENCES users(id),
    CONSTRAINT uq_att_student_date UNIQUE (student_id, attendance_date)
);
CREATE INDEX idx_att2_date ON attendance(attendance_date);

-- 20. medical_records : one medical info row per student (Feature 7)
CREATE TABLE medical_records (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id              BIGINT NOT NULL,
    allergy_info            VARCHAR(500),
    blood_type              ENUM('A','B','AB','O','UNKNOWN') NOT NULL DEFAULT 'UNKNOWN',
    emergency_contact_name  VARCHAR(120),
    emergency_contact_phone VARCHAR(30),
    updated_by              BIGINT,
    updated_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_med_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_med_updater FOREIGN KEY (updated_by) REFERENCES users(id),
    CONSTRAINT uq_med_student UNIQUE (student_id)
);

-- 21. medications : medication schedule per student (Feature 7)
CREATE TABLE medications (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT NOT NULL,
    medication_name VARCHAR(150) NOT NULL,
    dosage          VARCHAR(100) NOT NULL,
    schedule_time   VARCHAR(100),
    start_date      DATE,
    end_date        DATE,
    notes           VARCHAR(500),
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medi_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);
CREATE INDEX idx_medi_student ON medications(student_id);

-- 22. iep_goal_templates : reusable IEP goal bank (Feature 8)
CREATE TABLE iep_goal_templates (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain           ENUM('ACADEMIC','COMMUNICATION','SOCIAL_EMOTIONAL','MOTOR',
                          'BEHAVIORAL','SELF_CARE','VOCATIONAL') NOT NULL,
    goal_description VARCHAR(500) NOT NULL,
    success_criteria VARCHAR(500),
    suggested_rubric VARCHAR(500),
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tmpl_domain ON iep_goal_templates(domain, is_active);

-- 23. report_acknowledgements : parent acknowledgement of a published daily report (D22)
CREATE TABLE report_acknowledgements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id       BIGINT NOT NULL,
    parent_id       BIGINT NOT NULL,
    acknowledged_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ack_report_parent UNIQUE (report_id, parent_id),
    CONSTRAINT fk_ack_report FOREIGN KEY (report_id) REFERENCES daily_reports(id) ON DELETE CASCADE,
    CONSTRAINT fk_ack_parent FOREIGN KEY (parent_id) REFERENCES users(id) ON DELETE CASCADE
);
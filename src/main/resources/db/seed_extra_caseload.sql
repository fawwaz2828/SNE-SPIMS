-- ============================================================================
-- SNE-SPIMS — EXTRA CASELOAD top-up  (AUTO-ID, collision-safe)
--
-- Run on an ALREADY-SEEDED database (after schema.sql + seed.sql) to add 12
-- more students so each teacher carries a realistic class of 6-10:
--     James (id 2): Ryan + Daisy + Jacob + 6 new = 9 students (all Class A)
--     Laura (id 3): 6 new                        = 6 students (all Class B)
--
-- Unlike the explicit-id version, this script lets AUTO_INCREMENT assign every
-- id and wires up foreign keys via LAST_INSERT_ID(), so it is safe to run even
-- if rows 4,5,6... already exist.  Re-running it twice will fail on the unique
-- student_code (uq_students_code) — by design, to avoid duplicates.
--
--   mysql>  USE sne_spims;
--   mysql>  SOURCE src/main/resources/db/seed_extra_caseload.sql;
-- ============================================================================
USE sne_spims;

-- Each block: insert student -> capture @sid, then teacher link, IEP (@iid),
-- two goals, a daily report (@rid) and two rubrics. Teacher 2 = James, 3 = Laura.

-- ── 1. Ethan Brooks (James) ─────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-004','Ethan Brooks','2016-01-22','MALE','LEARNING','LOW','2023-08-01','Class A','Strong reader; needs support with maths.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Ethan 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Reads at grade level but struggles with number operations.','Extra time on maths tasks, manipulatives, chunked instructions.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'ACADEMIC','Add single-digit numbers with manipulatives',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',40),
 (@iid,'SELF_CARE','Organise own materials before each lesson',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',55);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Ethan added single-digit numbers using counters with growing confidence.','HAPPY','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Academic Engagement','DEVELOPING','Counters helped'),(@rid,'Emotional Regulation','PROFICIENT','');

-- ── 2. Mia Bennett (James) ──────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-005','Mia Bennett','2015-05-14','FEMALE','HEARING','MODERATE','2023-08-01','Class A','Uses a hearing aid; lip-reads well.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Mia 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Uses a hearing aid; lip-reads and follows visual cues well.','Preferential seating, face the student when speaking, captions.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'COMMUNICATION','Respond to questions using full sentences',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',50),
 (@iid,'SOCIAL_EMOTIONAL','Join a small-group activity each day',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',45);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Mia answered questions in full sentences during the morning lesson.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Communication','PROFICIENT','Full sentences'),(@rid,'Social Interaction','DEVELOPING','');

-- ── 3. Noah Carter (James) ──────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-006','Noah Carter','2017-02-09','MALE','AUTISM','HIGH','2024-01-10','Class A','Sensitive to noise; prefers quiet corners.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Noah 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 3 MONTH),DATE_ADD(CURDATE(),INTERVAL 9 MONTH),'Sensitive to noise; seeks quiet spaces when overwhelmed.','Noise-reducing headphones, quiet corner, predictable routine.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'BEHAVIORAL','Use a calm-down strategy when overwhelmed',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',35),
 (@iid,'COMMUNICATION','Request a break using a card or words',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',50);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 3 DAY),'Noah used his quiet corner to self-regulate before group time.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 3 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Emotional Regulation','DEVELOPING','Used quiet corner'),(@rid,'Social Interaction','EMERGING','');

-- ── 4. Olivia Hayes (James) ─────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-007','Olivia Hayes','2016-07-30','FEMALE','PHYSICAL','MODERATE','2023-09-05','Class A','Uses a walker; full classroom access.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Olivia 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Uses a walker; participates fully with mobility support.','Accessible seating, extra transition time, clear floor paths.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'MOTOR','Move between activities using the walker',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',60),
 (@iid,'SOCIAL_EMOTIONAL','Participate in group games with peers',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',55);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Olivia moved confidently between activities with her walker.','HAPPY','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Social Interaction','PROFICIENT','Joined group game'),(@rid,'Emotional Regulation','PROFICIENT','');

-- ── 5. Liam Foster (James) ──────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-008','Liam Foster','2015-12-03','MALE','SPEECH_LANGUAGE','LOW','2023-08-01','Class A','Expressive delay; benefits from picture cues.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Liam 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 3 MONTH),DATE_ADD(CURDATE(),INTERVAL 9 MONTH),'Expressive language delay; uses picture cues to communicate.','Picture cue cards, model-and-repeat prompts, small groups.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'COMMUNICATION','Use 3-4 word phrases to make requests',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',40),
 (@iid,'SOCIAL_EMOTIONAL','Greet a peer each morning',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',50);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 3 DAY),'Liam used picture cards to request the materials he needed.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 3 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Communication','DEVELOPING','Picture cards'),(@rid,'Academic Engagement','DEVELOPING','');

-- ── 6. Ava Reed (James) ─────────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-009','Ava Reed','2016-10-18','FEMALE','MULTIPLE','INTENSIVE','2024-02-12','Class A','1:1 support; complex needs.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (2,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Ava 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 5 MONTH),DATE_ADD(CURDATE(),INTERVAL 7 MONTH),'Complex needs; works with 1:1 support throughout the day.','1:1 aide, sensory breaks, simplified multi-step tasks.','ACTIVE',2);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'SELF_CARE','Follow a 2-step self-care routine with cues',DATE_ADD(CURDATE(),INTERVAL 4 MONTH),'IN_PROGRESS',30),
 (@iid,'COMMUNICATION','Indicate choice between two items',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',35);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,2,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Ava followed her self-care routine with support from her aide.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Emotional Regulation','DEVELOPING','With aide support'),(@rid,'Communication','EMERGING','');

-- ── 7. Lucas Gray (Laura) ───────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-010','Lucas Gray','2014-04-25','MALE','INTELLECTUAL','MODERATE','2022-08-15','Class B','Friendly; works best with routines.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Lucas 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Friendly and routine-driven; benefits from concrete materials.','First-then board, frequent breaks, concrete materials.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'ACADEMIC','Match numerals 1-10 to quantities',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',45),
 (@iid,'SELF_CARE','Pack own bag at end of day',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',60);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Lucas matched numerals to quantities up to 8 today.','HAPPY','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Academic Engagement','PROFICIENT','Matched to 8'),(@rid,'Emotional Regulation','PROFICIENT','');

-- ── 8. Sophia Price (Laura) ─────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-011','Sophia Price','2014-08-11','FEMALE','VISUAL','HIGH','2023-01-09','Class B','Uses large-print materials.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Sophia 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Low vision; reads with large-print and high-contrast materials.','Large-print materials, high-contrast handouts, front seating.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'ACADEMIC','Read large-print text for 10 minutes',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',55),
 (@iid,'SELF_CARE','Locate own belongings independently',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',50);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 3 DAY),'Sophia read large-print text for ten minutes without fatigue.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 3 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Academic Engagement','PROFICIENT','Sustained reading'),(@rid,'Emotional Regulation','PROFICIENT','');

-- ── 9. Mason Ward (Laura) ───────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-012','Mason Ward','2015-03-07','MALE','AUTISM','MODERATE','2023-08-01','Class B','Responds well to first-then boards.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Mason 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 3 MONTH),DATE_ADD(CURDATE(),INTERVAL 9 MONTH),'Responds well to first-then boards and visual schedules.','Visual schedule, first-then board, movement breaks.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'BEHAVIORAL','Follow the first-then board for transitions',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',50),
 (@iid,'COMMUNICATION','Use words to ask for help',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',45);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Mason followed his first-then board through every transition.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Emotional Regulation','PROFICIENT','Smooth transitions'),(@rid,'Communication','DEVELOPING','');

-- ── 10. Isla Hughes (Laura) ─────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-013','Isla Hughes','2014-11-29','FEMALE','LEARNING','LOW','2022-09-01','Class B','Confident; needs extended time on tasks.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Isla 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 5 MONTH),DATE_ADD(CURDATE(),INTERVAL 7 MONTH),'Confident learner; needs extended time to process tasks.','Extended time, reduced task load, checklists.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'ACADEMIC','Complete a task within the extended time',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',60),
 (@iid,'SOCIAL_EMOTIONAL','Ask a peer for help when stuck',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',55);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 3 DAY),'Isla completed her task within the extended time independently.','HAPPY','PUBLISHED',DATE_SUB(NOW(),INTERVAL 3 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Academic Engagement','PROFICIENT','Independent'),(@rid,'Social Interaction','DEVELOPING','');

-- ── 11. James Bell (Laura) ──────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-014','James Bell','2015-06-16','MALE','SPEECH_LANGUAGE','MODERATE','2023-10-02','Class B','Working on sentence length.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP James B 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 3 MONTH),DATE_ADD(CURDATE(),INTERVAL 9 MONTH),'Speaks in short phrases; working on sentence length.','Sentence starters, model-and-expand prompts, picture cues.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'COMMUNICATION','Produce 5-word sentences with prompting',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',45),
 (@iid,'SOCIAL_EMOTIONAL','Take turns in a conversation',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',50);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'James produced several five-word sentences during circle time.','CALM','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Communication','DEVELOPING','5-word sentences'),(@rid,'Social Interaction','DEVELOPING','');

-- ── 12. Grace Morgan (Laura) ────────────────────────────────────────────────
INSERT INTO students (student_code, full_name, date_of_birth, gender, disability_type, support_level, enrollment_date, class_group, notes, active)
 VALUES ('SNE-015','Grace Morgan','2014-02-21','FEMALE','PHYSICAL','HIGH','2022-08-15','Class B','Wheelchair user; very independent.',TRUE);
SET @sid := LAST_INSERT_ID();
INSERT INTO teacher_student (teacher_id, student_id, is_primary) VALUES (3,@sid,TRUE);
INSERT INTO iep_programs (student_id, title, academic_year, start_date, end_date, present_levels, accommodations, status, created_by)
 VALUES (@sid,'IEP Grace 2024/2025','2024/2025',DATE_SUB(CURDATE(),INTERVAL 4 MONTH),DATE_ADD(CURDATE(),INTERVAL 8 MONTH),'Wheelchair user; highly independent in academic work.','Accessible desk height, clear paths, extra transition time.','ACTIVE',3);
SET @iid := LAST_INSERT_ID();
INSERT INTO iep_goals (iep_id, domain, description, target_date, status, progress_pct) VALUES
 (@iid,'MOTOR','Navigate the classroom independently',DATE_ADD(CURDATE(),INTERVAL 3 MONTH),'IN_PROGRESS',70),
 (@iid,'ACADEMIC','Complete written work at grade level',DATE_ADD(CURDATE(),INTERVAL 2 MONTH),'IN_PROGRESS',65);
INSERT INTO daily_reports (student_id, teacher_id, report_date, summary, mood, status, published_at)
 VALUES (@sid,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'Grace navigated the classroom independently and finished her work.','HAPPY','PUBLISHED',DATE_SUB(NOW(),INTERVAL 2 DAY));
SET @rid := LAST_INSERT_ID();
INSERT INTO report_rubrics (report_id, criterion, rating, comment) VALUES
 (@rid,'Academic Engagement','PROFICIENT','Independent work'),(@rid,'Emotional Regulation','PROFICIENT','');
